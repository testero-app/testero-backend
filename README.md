# testero-backend

Backend for **Testero**, an open source system for administering tests
and assessments, designed for educational settings: private schools,
training organizations, teachers, and trainers.

This repository contains the **backend API**. The web frontend lives in
[testero-web](https://github.com/testero-app/testero-web).

> ⚠️ This repository is under initial development. The backend is being
> built with Spring Boot (Java), replacing an earlier Python prototype.

## Stack

- **Framework**: Spring Boot
- **Language**: Java
- **Database**: PostgreSQL (the hosted project uses [Supabase](https://supabase.com), but any PostgreSQL instance works)
- **Hosting**: [Render](https://render.com) via Docker (any container-compatible platform works)
- **Build**: Maven (wrapper included)

## Getting Started

Prerequisites:

- JDK 21 or later

```bash
# Clone the repository
git clone https://github.com/testero-app/testero-backend.git
cd testero-backend

# Copy the environment variables template and fill in the values
cp .env.example .env

# Run the application
./mvnw spring-boot:run
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | PostgreSQL JDBC connection string (Supabase) |
| `JWT_SECRET` | HS256 signing key, min 256 bits — generate with `openssl rand -hex 32` |
| `CORS_ORIGINS` | Allowed frontend origin (e.g. `http://localhost:3000`) |

See [`.env.example`](./.env.example) for the expected format.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](./CONTRIBUTING.md)
before opening a pull request. All contributions follow the
Developer Certificate of Origin (DCO) model.

## License

Released under the
[GNU Affero General Public License v3.0](./LICENSE).

This means anyone can use, modify, and redistribute the software, as long
as modified versions remain under the same license and the source code is
made available — including when the software is offered as a network service.

## Website

[testero.app](https://testero.app)
