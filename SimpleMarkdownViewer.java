package com.example.application.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.Getter;

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
    /**
     * -- GETTER --
     *  Gets the current markdown content
     *
     * @return The markdown content string
     */
    @Getter
    private String markdownContent = "";

    public SimpleMarkdownViewer() {
        // Configure flexmark options
        MutableDataSet options = new MutableDataSet();

        // Enable comprehensive markdown extensions
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
                // GitHub Flavored Markdown
                com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create(),
                com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create(),

                // Tables
                com.vladsch.flexmark.ext.tables.TablesExtension.create(),

                // Links and URLs
                com.vladsch.flexmark.ext.autolink.AutolinkExtension.create(),
                com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension.create(),

                // Code and syntax highlighting
                com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension.create(),
                com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension.create(),

                // Typography
                com.vladsch.flexmark.ext.typographic.TypographicExtension.create(),
                com.vladsch.flexmark.ext.superscript.SuperscriptExtension.create(),
                com.vladsch.flexmark.ext.ins.InsExtension.create(),

                // Lists and definitions
                com.vladsch.flexmark.ext.definition.DefinitionExtension.create(),
                com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension.create(),

                // Footnotes
                com.vladsch.flexmark.ext.footnotes.FootnoteExtension.create(),

                // Media
                com.vladsch.flexmark.ext.media.tags.MediaTagsExtension.create(),

                // Admonitions (callouts/alerts)
                com.vladsch.flexmark.ext.admonition.AdmonitionExtension.create(),

                // Mathematical expressions (if needed)
                com.vladsch.flexmark.ext.gitlab.GitLabExtension.create(),

                // Emoji support
                com.vladsch.flexmark.ext.emoji.EmojiExtension.create(),

                // YAML front matter
                com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension.create(),

                // Table of Contents
                com.vladsch.flexmark.ext.toc.TocExtension.create(),

                // Wikilinks
                com.vladsch.flexmark.ext.wikilink.WikiLinkExtension.create(),

                // Attributes (for custom styling)
                com.vladsch.flexmark.ext.attributes.AttributesExtension.create(),

                // Macros
                com.vladsch.flexmark.ext.macros.MacrosExtension.create()
        ));

        // Configure table options
        options.set(com.vladsch.flexmark.ext.tables.TablesExtension.COLUMN_SPANS, false)
                .set(com.vladsch.flexmark.ext.tables.TablesExtension.APPEND_MISSING_COLUMNS, true)
                .set(com.vladsch.flexmark.ext.tables.TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(com.vladsch.flexmark.ext.tables.TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);

        // Configure task list options
        options.set(com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.ITEM_DONE_MARKER, "<input type=\"checkbox\" checked disabled>")
                .set(com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.ITEM_NOT_DONE_MARKER, "<input type=\"checkbox\" disabled>");

        // Configure footnote options
        options.set(com.vladsch.flexmark.ext.footnotes.FootnoteExtension.FOOTNOTE_REF_PREFIX, "[")
                .set(com.vladsch.flexmark.ext.footnotes.FootnoteExtension.FOOTNOTE_REF_SUFFIX, "]");

        // Configure anchor link options
        options.set(com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor-link");

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

        // Add comprehensive markdown styling
        getElement().executeJs("""
            const style = document.createElement('style');
            style.textContent = `
                .markdown-viewer h1, .markdown-viewer h2, .markdown-viewer h3,
                .markdown-viewer h4, .markdown-viewer h5, .markdown-viewer h6 {
                    margin-top: 1.5rem;
                    margin-bottom: 0.5rem;
                    font-weight: bold;
                    line-height: 1.25;
                }
                .markdown-viewer h1 { font-size: 2rem; border-bottom: 1px solid var(--lumo-contrast-20pct); padding-bottom: 0.3rem; }
                .markdown-viewer h2 { font-size: 1.5rem; border-bottom: 1px solid var(--lumo-contrast-10pct); padding-bottom: 0.3rem; }
                .markdown-viewer h3 { font-size: 1.25rem; }
                .markdown-viewer h4 { font-size: 1rem; }
                .markdown-viewer h5 { font-size: 0.875rem; }
                .markdown-viewer h6 { font-size: 0.75rem; color: var(--lumo-secondary-text-color); }
                
                /* Anchor links */
                .markdown-viewer .anchor-link {
                    opacity: 0;
                    margin-left: 0.5rem;
                    text-decoration: none;
                    color: var(--lumo-primary-color);
                }
                .markdown-viewer h1:hover .anchor-link,
                .markdown-viewer h2:hover .anchor-link,
                .markdown-viewer h3:hover .anchor-link,
                .markdown-viewer h4:hover .anchor-link,
                .markdown-viewer h5:hover .anchor-link,
                .markdown-viewer h6:hover .anchor-link {
                    opacity: 1;
                }
                
                /* Text styling */
                .markdown-viewer p { margin-bottom: 1rem; }
                .markdown-viewer em { font-style: italic; }
                .markdown-viewer strong { font-weight: bold; }
                .markdown-viewer del { text-decoration: line-through; }
                .markdown-viewer ins { text-decoration: underline; background-color: var(--lumo-success-color-10pct); }
                .markdown-viewer sup { vertical-align: super; font-size: smaller; }
                .markdown-viewer sub { vertical-align: sub; font-size: smaller; }
                
                /* Code styling */
                .markdown-viewer code {
                    background-color: var(--lumo-contrast-5pct);
                    padding: 0.2rem 0.4rem;
                    border-radius: 3px;
                    font-family: 'SFMono-Regular', 'Consolas', 'Liberation Mono', 'Menlo', monospace;
                    font-size: 0.85em;
                    color: var(--lumo-primary-text-color);
                }
                .markdown-viewer pre {
                    background-color: var(--lumo-contrast-5pct);
                    padding: 1rem;
                    border-radius: var(--lumo-border-radius-m);
                    overflow-x: auto;
                    margin: 1rem 0;
                    border: 1px solid var(--lumo-contrast-10pct);
                }
                .markdown-viewer pre code {
                    background: none;
                    padding: 0;
                    border-radius: 0;
                }
                
                /* Blockquotes */
                .markdown-viewer blockquote {
                    border-left: 4px solid var(--lumo-primary-color);
                    padding: 0.5rem 1rem;
                    margin: 1rem 0;
                    background-color: var(--lumo-contrast-5pct);
                    font-style: italic;
                    color: var(--lumo-secondary-text-color);
                }
                .markdown-viewer blockquote p:last-child {
                    margin-bottom: 0;
                }
                
                /* Lists */
                .markdown-viewer ul, .markdown-viewer ol {
                    padding-left: 2rem;
                    margin: 1rem 0;
                }
                .markdown-viewer li {
                    margin: 0.25rem 0;
                }
                .markdown-viewer li p {
                    margin: 0.5rem 0;
                }
                
                /* Task lists */
                .markdown-viewer .task-list-item {
                    list-style: none;
                    margin-left: -1.5rem;
                }
                .markdown-viewer .task-list-item input[type="checkbox"] {
                    margin-right: 0.5rem;
                }
                
                /* Definition lists */
                .markdown-viewer dl {
                    margin: 1rem 0;
                }
                .markdown-viewer dt {
                    font-weight: bold;
                    margin-top: 1rem;
                }
                .markdown-viewer dd {
                    margin-left: 2rem;
                    margin-bottom: 1rem;
                }
                
                /* Tables */
                .markdown-viewer table {
                    border-collapse: collapse;
                    width: 100%;
                    margin: 1rem 0;
                    border: 1px solid var(--lumo-contrast-20pct);
                }
                .markdown-viewer th, .markdown-viewer td {
                    border: 1px solid var(--lumo-contrast-20pct);
                    padding: 0.75rem;
                    text-align: left;
                }
                .markdown-viewer th {
                    background-color: var(--lumo-contrast-5pct);
                    font-weight: bold;
                }
                .markdown-viewer tr:nth-child(even) {
                    background-color: var(--lumo-contrast-5pct);
                }
                
                /* Links */
                .markdown-viewer a {
                    color: var(--lumo-primary-text-color);
                    text-decoration: none;
                }
                .markdown-viewer a:hover {
                    text-decoration: underline;
                }
                
                /* Images */
                .markdown-viewer img {
                    max-width: 100%;
                    height: auto;
                    border-radius: var(--lumo-border-radius-s);
                }
                
                /* Horizontal rules */
                .markdown-viewer hr {
                    border: none;
                    border-top: 2px solid var(--lumo-contrast-20pct);
                    margin: 2rem 0;
                }
                
                /* Footnotes */
                .markdown-viewer .footnote-ref {
                    color: var(--lumo-primary-color);
                    text-decoration: none;
                    font-size: 0.8em;
                    vertical-align: super;
                }
                .markdown-viewer .footnote-ref:hover {
                    text-decoration: underline;
                }
                .markdown-viewer .footnotes {
                    margin-top: 2rem;
                    padding-top: 1rem;
                    border-top: 1px solid var(--lumo-contrast-20pct);
                    font-size: 0.9em;
                }
                .markdown-viewer .footnotes ol {
                    margin: 0;
                }
                
                /* Abbreviations */
                .markdown-viewer abbr {
                    border-bottom: 1px dotted var(--lumo-contrast-50pct);
                    cursor: help;
                }
                
                /* Admonitions/Callouts */
                .markdown-viewer .admonition {
                    margin: 1rem 0;
                    padding: 1rem;
                    border-radius: var(--lumo-border-radius-m);
                    border-left: 4px solid;
                }
                .markdown-viewer .admonition.note {
                    background-color: var(--lumo-primary-color-10pct);
                    border-left-color: var(--lumo-primary-color);
                }
                .markdown-viewer .admonition.warning {
                    background-color: var(--lumo-warning-color-10pct);
                    border-left-color: var(--lumo-warning-color);
                }
                .markdown-viewer .admonition.danger {
                    background-color: var(--lumo-error-color-10pct);
                    border-left-color: var(--lumo-error-color);
                }
                .markdown-viewer .admonition-title {
                    font-weight: bold;
                    margin-bottom: 0.5rem;
                }
                
                /* Table of Contents */
                .markdown-viewer .table-of-contents {
                    background-color: var(--lumo-contrast-5pct);
                    border: 1px solid var(--lumo-contrast-20pct);
                    border-radius: var(--lumo-border-radius-m);
                    padding: 1rem;
                    margin: 1rem 0;
                }
                .markdown-viewer .table-of-contents ul {
                    margin: 0;
                    padding-left: 1.5rem;
                }
                .markdown-viewer .table-of-contents > ul {
                    padding-left: 0;
                }
                
                /* Wiki links */
                .markdown-viewer .wiki-link {
                    color: var(--lumo-primary-color);
                    text-decoration: none;
                    background-color: var(--lumo-primary-color-10pct);
                    padding: 0.1rem 0.3rem;
                    border-radius: 3px;
                }
                .markdown-viewer .wiki-link:hover {
                    background-color: var(--lumo-primary-color-20pct);
                }
                
                /* Media tags */
                .markdown-viewer .video-container {
                    position: relative;
                    padding-bottom: 56.25%;
                    height: 0;
                    overflow: hidden;
                    margin: 1rem 0;
                }
                .markdown-viewer .video-container iframe {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
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
     * Clears the markdown content
     */
    public void clear() {
        setMarkdownContent("");
    }
}
