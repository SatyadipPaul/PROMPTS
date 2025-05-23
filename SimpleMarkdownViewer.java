package com.example.components;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Simple Vaadin component for displaying markdown using server-side rendering
 * Requires flexmark-java dependency in pom.xml:
 * 
 * <dependency>
 *     <groupId>com.vladsch.flexmark</groupId>
 *     <artifactId>flexmark-all</artifactId>
 *     <version>0.64.8</version>
 * </dependency>
 */
public class SimpleMarkdownViewer extends Div {
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    private String markdownContent = "";
    
    public SimpleMarkdownViewer() {
        // Configure flexmark options
        MutableDataSet options = new MutableDataSet();
        
        // Enable tables, strikethrough, autolinks, etc.
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
            com.vladsch.flexmark.ext.tables.TablesExtension.create(),
            com.vladsch.flexmark.ext.strikethrough.StrikethroughExtension.create(),
            com.vladsch.flexmark.ext.autolink.AutolinkExtension.create(),
            com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create()
        ));
        
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
        
        // Set default styling
        addClassName("markdown-viewer");
        getStyle()
            .set("padding", "1rem")
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("background-color", "var(--lumo-base-color)")
            .set("overflow", "auto");
        
        // Add some basic markdown styling
        getElement().executeJs("""
            const style = document.createElement('style');
            style.textContent = `
                .markdown-viewer h1, .markdown-viewer h2, .markdown-viewer h3,
                .markdown-viewer h4, .markdown-viewer h5, .markdown-viewer h6 {
                    margin-top: 1.5rem;
                    margin-bottom: 0.5rem;
                    font-weight: bold;
                }
                .markdown-viewer h1 { font-size: 2rem; }
                .markdown-viewer h2 { font-size: 1.5rem; }
                .markdown-viewer h3 { font-size: 1.25rem; }
                .markdown-viewer code {
                    background-color: var(--lumo-contrast-5pct);
                    padding: 0.2rem 0.4rem;
                    border-radius: 3px;
                    font-family: monospace;
                }
                .markdown-viewer pre {
                    background-color: var(--lumo-contrast-5pct);
                    padding: 1rem;
                    border-radius: var(--lumo-border-radius-m);
                    overflow-x: auto;
                }
                .markdown-viewer blockquote {
                    border-left: 4px solid var(--lumo-primary-color);
                    padding-left: 1rem;
                    margin-left: 0;
                    font-style: italic;
                }
                .markdown-viewer table {
                    border-collapse: collapse;
                    width: 100%;
                    margin: 1rem 0;
                }
                .markdown-viewer th, .markdown-viewer td {
                    border: 1px solid var(--lumo-contrast-20pct);
                    padding: 0.5rem;
                    text-align: left;
                }
                .markdown-viewer th {
                    background-color: var(--lumo-contrast-5pct);
                    font-weight: bold;
                }
            `;
            document.head.appendChild(style);
            """);
    }
    
    public SimpleMarkdownViewer(String markdownContent) {
        this();
        setMarkdownContent(markdownContent);
    }
    
    /**
     * Sets the markdown content to be displayed
     * @param markdownContent The markdown text to render
     */
    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent != null ? markdownContent : "";
        
        if (this.markdownContent.isEmpty()) {
            removeAll();
            return;
        }
        
        try {
            // Parse and render markdown
            Document document = parser.parse(this.markdownContent);
            String html = renderer.render(document);
            
            // Clear existing content and add rendered HTML
            removeAll();
            add(new Html(html));
            
        } catch (Exception e) {
            // Fallback to plain text if parsing fails
            removeAll();
            add(new Html("<p>Error rendering markdown: " + e.getMessage() + "</p>"));
        }
    }
    
    /**
     * Gets the current markdown content
     * @return The markdown content string
     */
    public String getMarkdownContent() {
        return markdownContent;
    }
    
    /**
     * Clears the markdown content
     */
    public void clear() {
        setMarkdownContent("");
    }
}
