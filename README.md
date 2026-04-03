# Song API - Deploy to Render + Supabase Postgres + Test in Postman

## 1) Prerequisites
- GitHub repository with this project pushed
- Render account
- Postman installed

## 2) Deploy using Render Blueprint (recommended)
This repo includes `render.yaml` to provision a **Docker web service** (`song-api`).

Because you're using **Supabase Postgres** (external DB), the blueprint does **not** create a Render-managed database. You will provide the DB credentials as environment variables in Render.

Steps:
1. Push this repo to GitHub (already done in your case).
2. In Render dashboard, click **New +** -> **Blueprint**.
3. Connect your GitHub repo and select this project.
4. Render detects `render.yaml`; confirm and deploy.
5. Wait until both the database and web service are green.

Before the first deploy finishes, set these env vars on the `song-api` service (Render dashboard -> Service -> Environment):
- `DB_HOST`
- `DB_PORT` (usually `5432`)
- `DB_NAME` (your Supabase database name)
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_PARAMS` (keep the default `?sslmode=require`)

## 3) If you deploy manually (without blueprint)
1. Create a **Docker Web Service** from this repo.
2. Set env vars on the Web Service:
  - `DB_HOST`, `DB_PORT`, `DB_NAME`
  - `DB_USERNAME`, `DB_PASSWORD`
  - `DB_PARAMS=?sslmode=require`

Your app should be reachable at:
- `https://<your-render-service>.onrender.com`

## 4) Confirm app is running
Open this in browser (or Postman):
- `GET https://<your-render-service>.onrender.com/magat/songs`

If it returns `[]` (or a JSON list), deployment is good.

## 5) Postman test collection (manual)
Set a Postman variable:
- `baseUrl = https://<your-render-service>.onrender.com`

### A. Create song
- **Method**: `POST`
- **URL**: `{{baseUrl}}/magat/songs`
- **Body (raw JSON)**:
```json
{
  "title": "Shape of You",
  "artist": "Ed Sheeran",
  "album": "Divide",
  "genre": "Pop",
  "url": "https://example.com/shape-of-you"
}
```
- Expected: `200 OK` with created song JSON (including `id`)

### B. Get all songs
- **Method**: `GET`
- **URL**: `{{baseUrl}}/magat/songs`
- Expected: list of songs

### C. Get by ID
- **Method**: `GET`
- **URL**: `{{baseUrl}}/magat/songs/1`
- Expected: song JSON or `404`

### D. Update song
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/magat/songs/1`
- **Body (raw JSON)**:
```json
{
  "title": "Shape of You (Updated)",
  "artist": "Ed Sheeran",
  "album": "Divide",
  "genre": "Pop",
  "url": "https://example.com/shape-of-you-updated"
}
```
- Expected: `200 OK` with updated object

### E. Search songs
- **Method**: `GET`
- **URL**: `{{baseUrl}}/magat/songs/search/shape`
- Expected: matching list (possibly empty list)

### F. Delete song
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/magat/songs/1`
- Expected: `200 OK` and delete message

## 6) Common Render issues
- **Connection refused / DB auth failed**:
  - Recheck `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- **App boots but tables missing**:
  - Ensure `spring.jpa.hibernate.ddl-auto=update` in `application.properties`
- **503 first request**:
  - Free instances may sleep; retry after wake-up

## 7) Supabase notes
- Important for Render: the Supabase **direct** host (`db.<project-ref>.supabase.co`) may be **IPv6-only** (no IPv4 A record). Render often has no IPv6 egress, which causes DB connection failures.
- Fix: use Supabase **Connection Pooling / Pooler** connection details (host/port/user/password/db) from Supabase project settings instead of the direct host.
  - In Supabase dashboard: **Project Settings → Database → Connection string → Connection pooling** (or “Pooler”).
  - Copy the **Host**, **Port**, **Database**, and **User** values shown there into Render env vars.
- Supabase requires SSL; this repo defaults `DB_PARAMS` to `?sslmode=require` for Render.

## 8) Optional local run with same config style
Use your local PostgreSQL and keep these environment variables:
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=db_song`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=admin`

Then run:
- `./mvnw spring-boot:run` (Linux/macOS)
- `mvnw.cmd spring-boot:run` (Windows)
