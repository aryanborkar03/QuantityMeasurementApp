# Render Deployment

This repo is a Spring Boot multi-module backend. For Render's free tier, deploy each Spring Boot process as a public web service:

- `qma-api-gateway`: public web service
- `qma-auth-service`: public web service
- `qma-service`: public web service
- `qma-redis`: Render Key Value instance

The `render.yaml` blueprint builds each Java module from the shared `Dockerfile` by setting the `MODULE` environment variable. Render also passes environment variables as Docker build args, so `MODULE=api-gateway`, `MODULE=auth-service`, and `MODULE=qma-service` select the correct jar at build time.

## Before Deploying

Create a production MySQL database outside this blueprint. The auth service already expects MySQL in prod mode.

Good options:

- Aiven MySQL
- Railway MySQL
- PlanetScale-compatible MySQL
- a Render private MySQL service with a persistent disk

Keep the MySQL credentials ready:

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DATABASE`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`

## Deploy Steps

1. Push this repo to GitHub.
2. In Render, choose **New > Blueprint**.
3. Select this repository.
4. Render will detect `render.yaml`.
5. Fill the prompted secret values.
6. Deploy.
7. After deploy, confirm the service URLs. If Render gives either internal service a different URL than `https://qma-auth-service.onrender.com` or `https://qma-service.onrender.com`, update `AUTH_SERVICE_URI` and `QMA_SERVICE_URI` on `qma-api-gateway`.

## Values Render Will Ask For

Set these when the Blueprint prompts you:

- `CORS_ALLOWED_ORIGINS`: your frontend URL, for example `https://your-frontend.onrender.com`
- `MYSQL_HOST`: your MySQL host
- `MYSQL_DATABASE`: your database name
- `MYSQL_USERNAME`: your database user
- `MYSQL_PASSWORD`: your database password
- `MAIL_USERNAME`: SMTP username, usually your Gmail address
- `MAIL_PASSWORD`: SMTP app password
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`: `https://<your-gateway>.onrender.com/login/oauth2/code/google`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `GITHUB_REDIRECT_URI`: `https://<your-gateway>.onrender.com/login/oauth2/code/github`
- `OAUTH2_REDIRECT_URI`: your frontend URL after login

Render generates `JWT_SECRET` once and shares it between the gateway and auth service.

## OAuth Callback URLs

After the gateway service is created, copy its Render URL. In Google Cloud Console and GitHub OAuth app settings, add:

- `https://<your-gateway>.onrender.com/login/oauth2/code/google`
- `https://<your-gateway>.onrender.com/login/oauth2/code/github`

Then make sure the same URLs are set in Render as `GOOGLE_REDIRECT_URI` and `GITHUB_REDIRECT_URI`.

## Notes

- `api-gateway` is the main public API. Your frontend should call this service.
- `auth-service` and `qma-service` are also public on the free setup because Render private services require a paid plan.
- Free Render services can sleep when idle, so the first request after inactivity can be slow.
- Eureka and Spring Boot Admin remain usable locally with `docker-compose.yml`, but are disabled on Render to reduce service count and deployment complexity.
- `qma-service` currently uses in-memory H2 for its history repository, so quantity history will reset on service restart. Auth data is persistent through MySQL.
