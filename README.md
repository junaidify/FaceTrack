# FaceTrack

AI-powered classroom attendance system using facial recognition. Students face a webcam, the system identifies them via ArcFace embeddings, and attendance is logged automatically.

## Architecture

```
FaceTrack/
├── backend/          # Java 17 · Spring Boot 3.3 · PostgreSQL · JWT
├── FaceService/      # Python 3.11 · FastAPI · InsightFace (ArcFace)
├── Frontend/         # React 19 · TypeScript · Vite · Material-UI
└── docker-compose.yml
```

**Four services in Docker Compose:**

| Service | Port | Tech |
|---------|------|------|
| `postgres` | 5432 | PostgreSQL 16 |
| `face-service` | 8002 | Python + InsightFace (ArcFace model) |
| `backend` | 8000 | Spring Boot + Spring Data JPA |
| `frontend` | 3000 | React + Nginx reverse proxy |

## Quick Start

```bash
docker-compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8000
- FaceService: http://localhost:8002/healthz
- Default admin: `admin@facetrack.local` / `admin123`

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/login` | Public | Login, get JWT |
| GET/POST/DELETE | `/api/v1/users` | Admin | Manage users |
| GET/POST/PATCH/DELETE | `/api/v1/classes` | Admin/Teacher | Class CRUD |
| GET/POST/PATCH/DELETE | `/api/v1/students` | Admin | Student roster |
| POST | `/api/v1/students/{id}/enroll-face` | Admin | Enroll face embedding |
| GET/POST | `/api/v1/sessions` | Auth | Attendance session windows |
| POST | `/api/v1/sessions/{id}/end` | Auth | End session, auto-mark absent |
| POST | `/api/v1/attendance/mark` | Auth | Submit webcam image for matching |
| GET | `/api/v1/reports/sessions/{id}` | Auth | Session attendance report |
| GET | `/api/v1/reports/sessions/{id}/excel` | Auth | Download Excel report |

## How It Works

1. Admin enrolls students with a face photo (generates 512-d ArcFace embedding stored in PostgreSQL as jsonb)
2. Teacher starts an attendance session for a class
3. Students face the webcam, image sent to backend
4. Backend forwards to FaceService for embedding + matching against enrolled faces
5. Match found (cosine distance <= 0.45) = marked PRESENT
6. Teacher ends session = unmarked students auto-marked ABSENT
7. Excel reports available per session

## Environment Variables

See `backend/src/main/resources/application.yml` and `FaceService/.env.example` for all configuration options.

Key variables: `JWT_SECRET`, `POSTGRES_URL`, `FACE_SERVICE_URL`, `FACE_MATCH_THRESHOLD`

---

Built by Junaid Khan
