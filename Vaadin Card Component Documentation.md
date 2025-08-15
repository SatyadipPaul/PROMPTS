# Vaadin Card Component Documentation

## Implementation




The Card component is a versatile container for grouping related content and actions, with several customization options for layout and appearance.

### Key Features

*   **Multiple slots:** Flexible content placement with distinct slots for media, title, subtitle, header, footer, and more.
*   **Customizable appearance:** Extensive theming options for shadows, borders, and backgrounds.

### Slots

The Card component provides the following slots:

#### Content Slot

Place any content here, and as many elements as needed.

```java
Card card = new Card();
Paragraph p1 = new Paragraph(
        "Lapland is the northern-most region of Finland and an active outdoor " +
        "destination.");
Paragraph p2 = new Paragraph(
        "The land of the indigenous Sámi people, known as Sámi homeland or " +
        "Sápmi, also crosses the northern part of the region.");
card.add(p1, p2);
```

#### Media

Place for an image, video, or an icon. Can also be used for other content, but it’s intended for illustration purposes. Displays before all other content in the card.

Use the `stretch-media` and `cover-media` style variants to control how media content is sized.

```java
// Card with image media
Card imageCard = new Card();
DownloadHandler imageHandler = DownloadHandler.forClassResource(
        getClass(), "/images/lapland.avif", "lapland.avif");
Image image = new Image(imageHandler, "");
imageCard.setMedia(image);
imageCard.setWidth("100px");
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

#### Title

Textual content with predefined styling. Only one element can be placed in this slot.

```java
Card card = new Card();
card.setTitle(new Div("Lapland"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

#### Subtitle

Textual content with predefined styling, presented below the title element. Only one element can be placed in this slot. Should be used together with the title slot.

```java
Card card = new Card();
card.setSubtitle(new Div("The Exotic North"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

#### Header

Content placed at the top of the card. Can be used for custom headers.

```java
Card card = new Card();
card.setHeader(new Div("Custom Header"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

#### Header Prefix

Content placed before the header. Can be used for icons or other small elements.

```java
Card card = new Card();
card.setHeaderPrefix(new Icon(VaadinIcon.INFO));
card.setHeader(new Div("Custom Header"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

#### Header Suffix

Content placed after the header. Can be used for actions or other small elements.

```java
Card card = new Card();
card.setHeaderSuffix(new Button("Action"));
card.setHeader(new Div("Custom Header"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

#### Footer

Content placed at the bottom of the card. Can be used for actions or additional information.

```java
Card card = new Card();
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
card.setFooter(new Button("Read More"));
```

### Accessibility

Vaadin Card component follows the WAI-ARIA Authoring Practices Guide for accessible rich internet applications. Ensure proper semantic structure and provide alternative text for images and other non-text content.

## Styling

The Card component supports the following style variants:

### Default

This is the default card style.

```java
Card cardDefault = new Card();
```

### Outlined

Adds a solid outline around the card.

```java
Card cardOutlined = new Card();
cardOutlined.addThemeVariants(CardVariant.LUMO_OUTLINED);
```

### Elevated

This variant works better on a shaded background.

```java
Card cardElevated = new Card();
cardElevated.addThemeVariants(CardVariant.LUMO_ELEVATED);
```

### Horizontal

Place all card content on the side of the media element, if provided.

```java
Card card = new Card();
card.addThemeVariants(CardVariant.LUMO_HORIZONTAL);

card.setMedia(new Avatar("Lapland"));
card.setTitle(new Div("Lapland"));
card.setSubtitle(new Div("The Exotic North"));
card.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Stretch Media

Stretches the media element as wide – or tall, if combined with the horizontal variant – as the card, if the media element is an image, video, or an icon.

```java
// Card with stretched image
Card imageCard = new Card();
imageCard.addThemeVariants(CardVariant.LUMO_STRETCH_MEDIA);

DownloadHandler imageHandler = DownloadHandler.forClassResource(
        getClass(), "/images/lapland.avif", "lapland.avif");
Image image = new Image(imageHandler, "");
imageCard.setMedia(image);

imageCard.setTitle(new Div("Lapland"));
imageCard.setSubtitle(new Div("The Exotic North"));
imageCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");

// Card with stretched icon
Card iconCard = new Card();
iconCard.addThemeVariants(CardVariant.LUMO_STRETCH_MEDIA);

Icon icon = LumoIcon.PHOTO.create();
icon.getStyle()
        .setColor("var(--lumo-primary-color)")
        .setBackgroundColor("var(--lumo-primary-color-10pct)");
iconCard.setMedia(icon);

iconCard.setTitle(new Div("Lapland"));
iconCard.setSubtitle(new Div("The Exotic North"));
iconCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Cover Media

Similar to the stretch-media variant, but this variant allows the media element to also cover the padding area of the card. This variant overrides the stretch-media variant.

```java
// Card with cover image
Card imageCard = new Card();
imageCard.addThemeVariants(CardVariant.LUMO_COVER_MEDIA);

DownloadHandler imageHandler = DownloadHandler.forClassResource(
        getClass(), "/images/lapland.avif", "lapland.avif");
Image image = new Image(imageHandler, "");
imageCard.setMedia(image);

imageCard.setTitle(new Div("Lapland"));
imageCard.setSubtitle(new Div("The Exotic North"));
imageCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");

// Card with cover icon
Card iconCard = new Card();
iconCard.addThemeVariants(CardVariant.LUMO_COVER_MEDIA);

Icon icon = LumoIcon.PHOTO.create();
icon.getStyle()
        .setColor("var(--lumo-primary-color)")
        .setBackgroundColor("var(--lumo-primary-color-10pct)");
iconCard.setMedia(icon);

iconCard.setTitle(new Div("Lapland"));
iconCard.setSubtitle(new Div("The Exotic North"));
iconCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
```

### Combine Variants

You can combine all style variants together.

```java
Card card = new Card();
card.addThemeVariants(
        CardVariant.LUMO_OUTLINED,
        CardVariant.LUMO_ELEVATED,
        CardVariant.LUMO_HORIZONTAL,
        CardVariant.LUMO_COVER_MEDIA
);

DownloadHandler imageHandler = DownloadHandler.forClassResource(
        getClass(), "/images/lapland.avif", "lapland.avif");
Image image = new Image(imageHandler, "");
image.setWidth("200px");
card.setMedia(image);

card.setTitle(new Div("Lapland"));
card.setSubtitle(new Div("The Exotic North"));
card.add(new Div("Lapland is the northern-most region of Finland and an active outdoor destination."));
```

### Style Properties

The following style properties can be used in CSS stylesheets to customize the appearance of this component. To apply values to these properties globally in your application UI, place them in a CSS block using the `html {--} ` selector. See [Lumo Style Properties](https://vaadin.com/docs/latest/ds/customization/lumo-css-properties) for more information on style properties.

| Feature | Property | Default Value |
|---|---|---|
| Background | `--vaadin-card-background` | `--lumo-shade-5pct` |
| Box Shadow | `--vaadin-card-box-shadow` | `none` |
| Border Width | `--vaadin-card-border-width` | `0` |
| Border Color | `--vaadin-card-border-color` | `--lumo-contrast-20pct` |
| Border Radius | `--vaadin-card-border-radius` | `--lumo-border-radius-l` |
| Padding | `--vaadin-card-padding` | `--lumo-space-m` |
| Gap | `--vaadin-card-gap` | `--lumo-space-m` |
| Media Aspect Ratio | `--vaadin-card-media-aspect-ratio` | `16/9`, when the `stretch-media` or `cover-media` style variant is used without the `horizontal` style variant. |

### CSS Selectors

The following CSS selectors can be used in stylesheets to target the various parts and states of the component. See the [Styling documentation](https://vaadin.com/docs/latest/styling/overview) for more details on how to style components.

*   **Root element:** `vaadin-card`

#### Parts

| Part name | Description |
|---|---|
| `media` | The media slot container. |
| `header` | The header slot container. |
| `header-prefix` | The header prefix slot container. |
| `header-suffix` | The header suffix slot container. |
| `title` | The title slot container. |
| `subtitle` | The subtitle slot container. |
| `content` | The content slot container. |
| `footer` | The footer slot container. |

#### State attributes

| Attribute | Description |
|---|---|
| `theme~="outlined"` | Applied when the `LUMO_OUTLINED` theme variant is used. |
| `theme~="elevated"` | Applied when the `LUMO_ELEVATED` theme variant is used. |
| `theme~="horizontal"` | Applied when the `LUMO_HORIZONTAL` theme variant is used. |
| `theme~="stretch-media"` | Applied when the `LUMO_STRETCH_MEDIA` theme variant is used. |
| `theme~="cover-media"` | Applied when the `LUMO_COVER_MEDIA` theme variant is used. |



