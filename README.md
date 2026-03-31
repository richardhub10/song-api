# Song API - Deploy to Render (PostgreSQL) + Test in Postman

## 1) Prerequisites
- GitHub repository with this project pushed
- Render account
- Postman installed

## 2) Deploy using Render Blueprint (recommended)
This repo now includes `render.yaml` that provisions both:
- a PostgreSQL database (`song-api-db`)
- a Docker web service (`song-api`)

Steps:
1. Push this repo to GitHub (already done in your case).
2. In Render dashboard, click **New +** -> **Blueprint**.
3. Connect your GitHub repo and select this project.
4. Render detects `render.yaml`; confirm and deploy.
5. Wait until both the database and web service are green.

No manual DB env vars are needed with the blueprint because `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` are injected automatically.

## 3) If you deploy manually (without blueprint)
1. Create a PostgreSQL instance in Render.
2. Create a Docker Web Service from this repo.
3. Set env vars on the Web Service:
  - `DB_HOST`, `DB_PORT`, `DB_NAME`
  - `DB_USERNAME`, `DB_PASSWORD`

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

## 7) Optional local run with same config style
Use your local PostgreSQL and keep these environment variables:
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=db_song`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=admin`

Then run:
- `./mvnw spring-boot:run` (Linux/macOS)
- `mvnw.cmd spring-boot:run` (Windows)
