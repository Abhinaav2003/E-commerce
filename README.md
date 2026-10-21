<h1 align="center">Cheetah</h1>

<p align="center">
  <img src="https://skillicons.dev/icons?i=java,spring,hibernate,postgres,maven" alt="Tech stack icons" />
</p>

**Cheetah** is a fast, mobile-first online store. Customers can browse and search products, manage a cart, place orders and review their order history. Admins can add, edit and delete products. This repository contains the complete user interface: eight server-rendered Thymeleaf pages with a shared, responsive design.

**Created by Abhinav Tiwari.**

## About the project

The goal is a simple, dependable shopping experience that feels the same on a small phone and a large monitor.

- **Customers** land on a product grid, search by keyword, add items to a cart, adjust quantities, check out, and see past orders.
- **Admins** manage the catalogue from a product list and a single add/edit form.
- **Design** follows a mobile-first approach. Layouts start from a phone-sized screen and expand to tablets and desktops. The pages share one colour theme, one typeface and one set of components, so the site feels like a single product.

### User flow

```
Browse / search  ->  Add to cart  ->  Review cart  ->  Place order  ->  View orders
        |
        +--> Register / Log in (required to check out)

Admin: Log in  ->  Manage products  ->  Add / Edit / Delete
```

## Features

**Customer**
- Product grid with search, price in ₹, and stock badges ("Only 3 left", "Out of stock")
- Lazy-loaded product images with a clean fallback if an image fails
- Cart with quantity updates, remove, subtotal and total
- Order history with date, items and total
- Registration and login with show/hide password

**Admin**
- Product list: table on desktop, stacked cards on phones, with stock status
- Add/edit form with inline validation and a live image preview
- Delete with a confirmation prompt

**Quality**
- Responsive from about 320px wide up to large desktops
- 16px form inputs so iPhones don't zoom on focus
- Visible keyboard focus, labelled fields, and alerts that screen readers announce
- Empty states for no products, no search results, an empty cart and no orders

## Tech stack

### Front end (in this repository)

| Area | Technology |
| --- | --- |
| Templating | Thymeleaf (server-side rendering, `th:*` attributes) |
| UI framework | Bootstrap 5.3.3 (CSS and JS bundle, loaded from jsDelivr) |
| Styling | Custom CSS with CSS variables (inline per page, plus `style.css` for the storefront) |
| Typography | Plus Jakarta Sans (Google Fonts) |
| Scripting | Small amounts of vanilla JavaScript (password toggle, image preview) |

### Back end (expected by these templates)

The templates are written for a **Spring Boot** app. Based on the Thymeleaf expressions, form actions and CSRF handling they rely on:

| Area | Technology |
| --- | --- |
| Framework | Spring Boot with Spring MVC controllers |
| View layer | Thymeleaf |
| Security | Spring Security (form login at `/login`, POST logout, CSRF tokens) |
| Data | Not specified here. Use any store that provides the fields listed below (for example Spring Data JPA with a relational database). |

Adjust this table to match your actual project.

## Project structure

```
src/main/resources/
├── static/css/style.css          # theme for index.html
└── templates/
    ├── index.html                # storefront (uses style.css)
    ├── index-inline.html         # storefront with CSS inlined (alternative)
    ├── login.html
    ├── register.html
    ├── cart.html
    ├── orders.html
    ├── admin-products.html       # admin product list
    └── product-form.html         # admin add / edit form
```

Use either `index.html` with `style.css`, or `index-inline.html`, not both. Every other page carries its own inline CSS.

## Getting started

1. Copy `src/main/resources/templates/*` and `src/main/resources/static/css/style.css` into your Spring Boot project.
2. Make sure your controller view names match the file names (for example `admin-products` and `product-form`).
3. Run the app and open `http://localhost:8080/`.

Bootstrap and the font load from CDNs, so you need an internet connection unless you self-host them.

## Data the templates expect

| Object | Fields |
| --- | --- |
| Product | `id`, `name`, `description`, `price` (numeric), `stock` (integer), `imageUrl` |
| Cart item | `product`, `quantity`, `subtotal` (numeric) |
| Order | `id`, `createdAt`, `itemsSummary`, `total` (numeric) |

## Routes and model attributes

| Page | View | Model attributes |
| --- | --- | --- |
| `/` | `index` | `products`, `search` |
| `/login` | `login` | `param.error`, `param.registered`, `param.logout` |
| `/register` | `register` | `error` |
| `/cart` | `cart` | `items`, `total`, `error` |
| `/orders` | `orders` | `orders` |
| `/admin/products` | `admin-products` | `products` |
| `/admin/products/new`, `/{id}/edit` | `product-form` | `product`, `formTitle` |

Forms POST to `/login`, `/register`, `/logout`, `/cart/add/{id}`, `/cart/update/{id}`, `/cart/remove/{id}`, `/checkout`, `/admin/products`, `/admin/products/{id}` and `/admin/products/{id}/delete`. All use `th:action`, so Spring Security CSRF tokens are included automatically.

## Theming

Colours are CSS variables at the top of each page:

| Variable | Value | Use |
| --- | --- | --- |
| `--ink` | `#17212b` | Navbar, hero, footer, text |
| `--brand` | `#0f766e` | Buttons, links |
| `--accent` | `#ffb703` | Logo accent, highlights, focus ring |
| `--paper` | `#f5f7f8` | Page background |
| `--danger` | `#b42318` | Errors, delete actions |

To rebrand, change the variables and the `Chee<span>tah</span>` wordmark in each page (search for `navbar-brand` and `auth-brand`).

## Admin login

- URL: `/admin/login` (password only)
- Default password: `Admin------` (account `admin@cheetah.com`)
- Change it by setting the `ADMIN_PASSWORD` environment variable (and `ADMIN_EMAIL` if needed). The password is re-synced on every startup.
- **Change the default before deploying publicly.**

## Notes

- Prices are formatted with `#numbers.formatDecimal`, so `price`, `subtotal` and `total` must be numeric.
- `order.createdAt` prints as-is. For a `LocalDateTime`, use `#temporals.format(order.createdAt, 'dd MMM yyyy, hh:mm a')`.
- Add Spring Security's `sec:authorize` to the navbar links so users only see the ones that apply, and show Admin to admins only.
- Restrict `/admin/**` to an admin role in your security configuration. Hiding a link in the UI does not protect the route.

## Roadmap

- Product categories and filters
- Order status updates (shipped, delivered)
- Product detail pages
- Payment integration
- Self-hosted Bootstrap and fonts for offline use

## Contributing

Issues and pull requests are welcome. Please keep pages mobile-first and test at phone, tablet and desktop widths before submitting.

## Author

Created by **Abhinav Tiwari**.

## License

Released under the [MIT License](LICENSE). Copyright (c) 2026 Abhinav Tiwari.