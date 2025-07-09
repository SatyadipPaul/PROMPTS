import { LitElement, html, css } from 'lit';

class SQLAutocompleteTextArea extends LitElement {
    static styles = css`
        :host {
            display: block;
            position: relative;
            width: 100%;
        }

        .container {
            position: relative;
            width: 100%;
        }

        textarea {
            width: 100%;
            min-height: 200px;
            font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
            font-size: 14px;
            line-height: 1.6;
            padding: 15px;
            border: 2px solid #ddd;
            border-radius: 6px;
            background-color: #fafafa;
            resize: vertical;
            outline: none;
            box-sizing: border-box;
        }

        textarea:focus {
            border-color: #007acc;
            background-color: white;
        }

        .suggestions-popup {
            position: absolute;
            background: white;
            border: 1px solid #ccc;
            border-radius: 6px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            max-height: 200px;
            overflow-y: auto;
            z-index: 1000;
            min-width: 200px;
            display: none;
            font-family: monospace;
            font-size: 14px;
        }

        .suggestion-item {
            padding: 10px 15px;
            cursor: pointer;
            border-bottom: 1px solid #f0f0f0;
            transition: background-color 0.1s ease;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .suggestion-item:hover {
            background-color: #f8f9fa;
        }

        .suggestion-item.selected {
            background-color: #007acc;
            color: white;
        }

        .suggestion-item:last-child {
            border-bottom: none;
        }

        .suggestion-type {
            font-size: 11px;
            opacity: 0.7;
            text-transform: uppercase;
            font-weight: bold;
        }

        .suggestion-item.selected .suggestion-type {
            opacity: 0.9;
        }
    `;

    static properties = {
        value: { type: String },
        placeholder: { type: String },
        readOnly: { type: Boolean },
        required: { type: Boolean }
    };

    constructor() {
        super();
        this.value = '';
        this.placeholder = '';
        this.readOnly = false;
        this.required = false;
        this.suggestions = [];
        this.selectedIndex = -1;
        this.currentWord = '';
        this.wordStart = 0;
        this.wordEnd = 0;
        this.tableColumns = {};
        this.customKeywords = [];

        this.sqlKeywords = [
            'SELECT', 'FROM', 'WHERE', 'INSERT', 'UPDATE', 'DELETE', 'CREATE', 'DROP',
            'ALTER', 'INDEX', 'TABLE', 'DATABASE', 'SCHEMA', 'VIEW', 'TRIGGER', 'FUNCTION',
            'PROCEDURE', 'IF', 'ELSE', 'CASE', 'WHEN', 'THEN', 'END', 'AS', 'ALIAS',
            'INNER', 'LEFT', 'RIGHT', 'FULL', 'OUTER', 'JOIN', 'ON', 'UNION', 'ALL',
            'GROUP', 'BY', 'ORDER', 'HAVING', 'LIMIT', 'OFFSET', 'DISTINCT', 'TOP',
            'AND', 'OR', 'NOT', 'IN', 'EXISTS', 'BETWEEN', 'LIKE', 'IS', 'NULL',
            'COUNT', 'SUM', 'AVG', 'MAX', 'MIN', 'SUBSTRING', 'CONCAT', 'UPPER', 'LOWER',
            'TRIM', 'LENGTH', 'CAST', 'CONVERT', 'COALESCE', 'ISNULL', 'DATEPART',
            'GETDATE', 'NOW', 'CURRENT_TIMESTAMP', 'VARCHAR', 'INT', 'DECIMAL', 'DATE',
            'DATETIME', 'TEXT', 'BOOLEAN', 'FLOAT', 'DOUBLE', 'CHAR', 'NVARCHAR'
        ];
    }

    render() {
        return html`
            <div class="container">
                <textarea
                    .value=${this.value}
                    .placeholder=${this.placeholder}
                    ?readonly=${this.readOnly}
                    ?required=${this.required}
                    @input=${this.handleInput}
                    @keydown=${this.handleKeyDown}
                    @blur=${this.handleBlur}
                    @focus=${this.handleFocus}
                    @scroll=${this.positionPopup}
                ></textarea>
                <div class="suggestions-popup" @mousedown=${this.handleMouseDown}>
                    ${this.suggestions.map((suggestion, index) => html`
                        <div
                            class="suggestion-item ${index === this.selectedIndex ? 'selected' : ''}"
                            @click=${() => this.selectSuggestion(index)}
                        >
                            <span>${suggestion.text}</span>
                            <span class="suggestion-type">${suggestion.type}</span>
                        </div>
                    `)}
                </div>
            </div>
        `;
    }

    handleInput(e) {
        this.value = e.target.value;
        this.dispatchEvent(new CustomEvent('value-changed', {
            detail: { value: this.value }
        }));

        const cursorPos = e.target.selectionStart;
        this.findCurrentWord(this.value, cursorPos);
        this.updateSuggestions();
    }

    handleKeyDown(e) {
        const popup = this.shadowRoot.querySelector('.suggestions-popup');
        if (popup.style.display === 'none') {
            return;
        }

        switch(e.key) {
            case 'ArrowDown':
                e.preventDefault();
                this.navigateSuggestions(1);
                break;
            case 'ArrowUp':
                e.preventDefault();
                this.navigateSuggestions(-1);
                break;
            case 'Tab':
                e.preventDefault();
                this.acceptSuggestion();
                break;
            case 'Enter':
                if (this.selectedIndex >= 0) {
                    e.preventDefault();
                    this.acceptSuggestion();
                }
                break;
            case 'Escape':
                this.hideSuggestions();
                break;
        }
    }

    handleBlur(e) {
        // Delay to allow clicking on suggestions
        setTimeout(() => this.hideSuggestions(), 150);
    }

    handleFocus(e) {
        if (this.currentWord.length >= 2) {
            this.updateSuggestions();
        }
    }

    handleMouseDown(e) {
        e.preventDefault(); // Prevent textarea from losing focus
    }

    findCurrentWord(text, cursorPos) {
        let start = cursorPos;
        while (start > 0 && this.isWordChar(text[start - 1])) {
            start--;
        }

        let end = cursorPos;
        while (end < text.length && this.isWordChar(text[end])) {
            end++;
        }

        this.currentWord = text.substring(start, end);
        this.wordStart = start;
        this.wordEnd = end;
    }

    isWordChar(char) {
        return /[a-zA-Z0-9_]/.test(char);
    }

    updateSuggestions() {
        if (this.currentWord.length < 2) {
            this.hideSuggestions();
            return;
        }

        const suggestions = this.getSuggestions(this.currentWord);

        if (suggestions.length > 0) {
            this.suggestions = suggestions;
            this.selectedIndex = 0;
            this.showSuggestions();
        } else {
            this.hideSuggestions();
        }
    }

    getSuggestions(word) {
        const lowerWord = word.toLowerCase();
        const suggestions = [];

        // Add SQL keywords
        this.sqlKeywords.forEach(keyword => {
            if (keyword.toLowerCase().startsWith(lowerWord)) {
                suggestions.push({
                    text: keyword,
                    type: 'keyword'
                });
            }
        });

        // Add custom keywords
        this.customKeywords.forEach(keyword => {
            if (keyword.toLowerCase().startsWith(lowerWord)) {
                suggestions.push({
                    text: keyword,
                    type: 'custom'
                });
            }
        });

        // Add table names
        Object.keys(this.tableColumns).forEach(table => {
            if (table.toLowerCase().startsWith(lowerWord)) {
                suggestions.push({
                    text: table,
                    type: 'table'
                });
            }
        });

        // Add column names with context
        const tableContext = this.getTableContext();
        if (tableContext && this.tableColumns[tableContext]) {
            this.tableColumns[tableContext].forEach(column => {
                if (column.toLowerCase().startsWith(lowerWord)) {
                    suggestions.push({
                        text: column,
                        type: 'column',
                        context: tableContext
                    });
                }
            });
        }

        // Add all column names
        Object.entries(this.tableColumns).forEach(([table, columns]) => {
            columns.forEach(column => {
                if (column.toLowerCase().startsWith(lowerWord)) {
                    suggestions.push({
                        text: column,
                        type: 'column',
                        context: table
                    });
                }
            });
        });

        // Remove duplicates and sort
        const uniqueSuggestions = suggestions.filter((item, index, self) =>
            index === self.findIndex(t => t.text === item.text && t.type === item.type)
        );

        return uniqueSuggestions.slice(0, 10);
    }

    getTableContext() {
        const query = this.value.toLowerCase();
        const words = query.split(/\s+/);

        for (let i = 0; i < words.length - 1; i++) {
            if (words[i] === 'from' || words[i] === 'join') {
                const tableName = words[i + 1].replace(/[^a-z0-9_]/g, '');
                if (this.tableColumns[tableName]) {
                    return tableName;
                }
            }
        }
        return null;
    }

    showSuggestions() {
        const popup = this.shadowRoot.querySelector('.suggestions-popup');
        popup.style.display = 'block';
        this.positionPopup();
        this.requestUpdate();
    }

    hideSuggestions() {
        const popup = this.shadowRoot.querySelector('.suggestions-popup');
        popup.style.display = 'none';
        this.selectedIndex = -1;
        this.requestUpdate();
    }

    navigateSuggestions(direction) {
        if (this.suggestions.length === 0) return;

        this.selectedIndex = Math.max(0, Math.min(
            this.suggestions.length - 1,
            this.selectedIndex + direction
        ));

        this.requestUpdate();

        // Scroll selected item into view
        this.updateComplete.then(() => {
            const selectedItem = this.shadowRoot.querySelector('.suggestion-item.selected');
            if (selectedItem) {
                selectedItem.scrollIntoView({ block: 'nearest' });
            }
        });
    }

    selectSuggestion(index) {
        this.selectedIndex = index;
        this.acceptSuggestion();
    }

    acceptSuggestion() {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.suggestions.length) {
            const suggestion = this.suggestions[this.selectedIndex];
            const beforeWord = this.value.substring(0, this.wordStart);
            const afterWord = this.value.substring(this.wordEnd);

            this.value = beforeWord + suggestion.text + afterWord;

            // Update textarea value
            const textarea = this.shadowRoot.querySelector('textarea');
            textarea.value = this.value;

            // Set cursor position
            const newCursorPos = this.wordStart + suggestion.text.length;
            textarea.setSelectionRange(newCursorPos, newCursorPos);

            this.hideSuggestions();
            textarea.focus();

            // Dispatch value change event
            this.dispatchEvent(new CustomEvent('value-changed', {
                detail: { value: this.value }
            }));
        }
    }

    positionPopup() {
        const textarea = this.shadowRoot.querySelector('textarea');
        const popup = this.shadowRoot.querySelector('.suggestions-popup');

        if (!textarea || !popup) return;

        const rect = textarea.getBoundingClientRect();
        const style = window.getComputedStyle(textarea);
        const lineHeight = parseInt(style.lineHeight);
        const fontSize = parseInt(style.fontSize);

        // Calculate cursor position
        const cursorPos = textarea.selectionStart;
        const textBeforeCursor = this.value.substring(0, cursorPos);
        const lines = textBeforeCursor.split('\n');
        const currentLine = lines.length - 1;
        const charInLine = lines[lines.length - 1].length;

        const charWidth = fontSize * 0.6;

        const top = (currentLine * lineHeight) + lineHeight + 5;
        const left = (charInLine * charWidth);

        popup.style.position = 'absolute';
        popup.style.top = top + 'px';
        popup.style.left = left + 'px';

        // Ensure popup stays within bounds
        const containerRect = this.getBoundingClientRect();
        const popupRect = popup.getBoundingClientRect();

        if (popupRect.bottom > containerRect.bottom + window.innerHeight) {
            popup.style.top = (top - popupRect.height - lineHeight - 10) + 'px';
        }
        if (popupRect.right > containerRect.right + window.innerWidth) {
            popup.style.left = (containerRect.width - popupRect.width - 10) + 'px';
        }
    }

    // Public methods for Java integration
    addSchemaTable(tableName, columns) {
        this.tableColumns[tableName] = columns;
    }

    clearSchema() {
        this.tableColumns = {};
    }

    addCustomKeywords(keywords) {
        this.customKeywords = [...keywords];
    }
}

customElements.define('sql-autocomplete-textarea', SQLAutocompleteTextArea);