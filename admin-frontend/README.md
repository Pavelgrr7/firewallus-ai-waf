# FirewallUs — Admin Frontend

React + Vite SPA for managing the FirewallUs AI WAF (Web Application Firewall).

## Stack

| Tool | Role |
|---|---|
| React 18 | UI framework |
| TypeScript | Type safety |
| Vite | Dev server & bundler |
| Tailwind CSS | Styling |
| Axios | HTTP client with JWT interceptor |
| React Router | Client-side routing |

## Development

```bash
npm install
npm run dev        # starts Vite dev server at http://localhost:5173
```

API requests are proxied through Vite to `http://localhost:8080` (Spring backend).
Make sure the backend container is running before starting the dev server:

```bash
# from project root
docker compose up -d backend-spring postgres redis kafka
```

## Production (Docker)

The frontend is served by nginx inside a Docker container on port 3001:

```bash
# from project root
docker compose up -d --build admin-frontend
```

nginx configuration (`nginx.conf`) handles:
- SPA fallback (`try_files` → `index.html`)
- Reverse proxy of `/api/` → `backend-spring:8080`
- Static asset caching (1 year, immutable)
- Security headers on all locations (`X-Frame-Options`, `X-Content-Type-Options`, etc.)

## Project Structure

```
src/
  components/   # Reusable UI components
  context/      # React context (AuthContext, etc.)
  pages/        # Route-level page components
  routes/       # Route definitions & guards
  services/     # API service layer (authService, etc.)
  data/         # Static/mock data
```

## Authentication

JWT-based. On login the token is stored in `localStorage` and attached to every outgoing
request via an Axios request interceptor. A 401 response automatically clears the token
and redirects to `/login`.
