***

```markdown
# Card Component (Vaadin)

The **Card component** is a versatile container for grouping related content and actions, with multiple options for layout and appearance.

## Key Features

- **Multiple slots**: Flexible placement for media, title, subtitle, header, footer, and more.
- **Customizable appearance**: Theming options for shadows, borders, backgrounds, etc.

---

## Slots

- **Content slot** — for the main content.
- **Media** — for images, videos, or icons.
- **Title** — for a main heading.
- **Subtitle** — for secondary heading.
- **Header** — for complete custom header content.
- **Header Prefix** — content before the header.
- **Header Suffix** — content after the header.
- **Footer** — for actions or additional content.

---

## Code Examples

### Content Slot

```
Card card = new Card(); 
Paragraph p1 = new Paragraph(
  "Lapland is the northern-most region of Finland and an active outdoor destination that's " +
  "known for its incredible, year-round light phenomena, vast arctic nature, and Santa Claus."
); 
Paragraph p2 = new Paragraph(
  "The land of the indigenous Sámi people, known as Sámi homeland or Sápmi, also crosses the " +
  "northern part of the region."
); 
card.add(p1, p2);
```

### Media

```
// Card with image media
Card imageCard = new Card();
DownloadHandler imageHandler = DownloadHandler.forClassResource(
  getClass(), "/images/lapland.avif", "lapland.avif"
);
Image image = new Image(imageHandler, "");
image.setWidth("100px");
imageCard.setMedia(image);
imageCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");

// Card with icon media
Card iconCard = new Card();
Icon icon = LumoIcon.PHOTO.create();
iconCard.setMedia(icon);
iconCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");

// Card with avatar media
Card avatarCard = new Card();
Avatar avatar = new Avatar("Lapland");
avatarCard.setMedia(avatar);
avatarCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Title

```
Card card = new Card();
card.setTitle(new Div("Lapland"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Subtitle

```
Card card = new Card();
card.setTitle(new Div("Lapland"));
card.setSubtitle(new Div("The Exotic North"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Header

```
Card card = new Card();
Div header = new Div();
header.addClassNames(
  LumoUtility.Display.FLEX,
  LumoUtility.FlexDirection.COLUMN_REVERSE,
  LumoUtility.LineHeight.XSMALL
);
H2 title = new H2("Lapland");
Div subtitle = new Div("The Exotic North");
subtitle.addClassNames(
  LumoUtility.TextTransform.UPPERCASE, 
  LumoUtility.FontSize.XSMALL, 
  LumoUtility.TextColor.SECONDARY
);
header.add(title, subtitle);
card.setHeader(header);
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Header Prefix

```
Card card = new Card();
card.setHeaderPrefix(new Avatar("Lapland"));
card.setTitle(new Div("Lapland"));
card.setSubtitle(new Div("The Exotic North"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Header Suffix

```
Card card = new Card();
card.setTitle(new Div("Lapland"));
card.setSubtitle(new Div("The Exotic North"));
Span badge = new Span("Arctic");
badge.getElement().getThemeList().add("badge success");
card.setHeaderSuffix(badge);
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Footer

```
Card card = new Card();
card.setTitle(new Div("Lapland"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
Button bookVacationButton = new Button("Book Vacation");
Button learnMoreButton = new Button("Learn More");
card.addToFooter(bookVacationButton, learnMoreButton);
```

---

## Accessibility Notes

- Default ARIA `role="region"`
- Title is given `role="heading"` and defaults to heading level 2
- Heading is linked to Card root with `aria-labelledby`
- Both `role` and heading level are customizable

**Example:**

```
card.setAriaRole("listitem");
card.setTitleHeadingLevel(3);
```

---

# Styling the Card Component

The Card component supports extensive styling and theming.

## Style Variants

- **Default**: The standard card style.
- **Outlined**: Adds a solid outline around the card.
- **Elevated**: Adds a shadow (works best on shaded backgrounds).
  ```
  Card cardDefault = new Card();
  Card cardOutlined = new Card();
  cardOutlined.addThemeVariants(CardVariant.LUMO_OUTLINED);
  Card cardElevated = new Card();
  cardElevated.addThemeVariants(CardVariant.LUMO_ELEVATED);
  ```
- **Horizontal**: Places all card content beside the media element.
  ```
  Card card = new Card();
  card.addThemeVariants(CardVariant.LUMO_HORIZONTAL);
  card.setMedia(new Avatar("Lapland"));
  card.setTitle(new Div("Lapland"));
  card.setSubtitle(new Div("The Exotic North"));
  card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
  ```
- **Stretch Media**: Stretches media as wide (or tall, if horizontal) as the card.
  ```
  Card imageCard = new Card();
  imageCard.addThemeVariants(CardVariant.LUMO_STRETCH_MEDIA);
  // set image media, title, and add content as shown above.
  ```
- **Cover Media**: Media covers the padding area of the card, overrides stretch-media.
  ```
  Card imageCard = new Card();
  imageCard.addThemeVariants(CardVariant.LUMO_COVER_MEDIA);
  // set image media, title, and add content as shown above.
  ```
- **Combine Variants**: You can combine multiple style variants.
  ```
  Card card = new Card();
  card.addThemeVariants(
    CardVariant.LUMO_OUTLINED,
    CardVariant.LUMO_ELEVATED,
    CardVariant.LUMO_HORIZONTAL,
    CardVariant.LUMO_COVER_MEDIA
  );
  // set image media, title, and add content as shown above.
  ```

---

## Style Properties (CSS Custom Properties)

Set these in your stylesheet—globally with `html { ... }` or scoped as needed:

| Feature           | Property Name                | Default Value           |
|-------------------|-----------------------------|------------------------|
| Background        | --vaadin-card-background     | --lumo-shade-5pct      |
| Box Shadow        | --vaadin-card-box-shadow     | none                   |
| Border Width      | --vaadin-card-border-width   | 0                      |
| Border Color      | --vaadin-card-border-color   | --lumo-contrast-20pct  |
| Border Radius     | --vaadin-card-border-radius  | --lumo-border-radius-l |
| Padding           | --vaadin-card-padding        | --lumo-space-m         |
| Gap               | --vaadin-card-gap            | --lumo-space-m         |
| Media Aspect Ratio| --vaadin-card-media-aspect-ratio | 16/9              |

---

## CSS Selectors

Target various parts and states of the component with selectors:

- **Root element**: `vaadin-card`
- **Parts**:
  - Media: `vaadin-card::part(media)`
  - Header: `vaadin-card::part(header)`
  - Content: `vaadin-card::part(content)`
  - Footer: `vaadin-card::part(footer)`
  - Title: `vaadin-card [slot="title"]`
- **Style Variants**:
  - Outlined: `vaadin-card[theme~="outlined"]`
  - Elevated: `vaadin-card[theme~="elevated"]`
  - Horizontal: `vaadin-card[theme~="horizontal"]`
  - Stretch Media: `vaadin-card[theme~="stretch-media"]`
  - Cover Media: `vaadin-card[theme~="cover-media"]`

---

For more advanced and custom styles, refer to the Lumo theme properties and the rest of the Vaadin styling documentation.
```

***

You can save this content as, for example, `vaadin-card-docs.md` for your reference. If you want programmatic help creating the file, I can guide you through generating one using a snippet in your preferred programming language.

[1] https://vaadin.com/docs/latest/components/card/styling
