# API reference

All requests below are sent to `http://localhost:8080`. Except authentication, supply `Authorization: Bearer <accessToken>`.

| Method | Path | Purpose | Typical response |
|---|---|---|---|
| POST | `/api/auth/signup` | Register | 201 / 409 |
| POST | `/api/auth/signin` | Login and issue JWT | 200 / 401 |
| POST | `/api/movies` | Create movie | 201 |
| GET | `/api/movies` | List movies | 200 |
| GET | `/api/movies/{id}` | Get movie | 200 / 404 |
| PUT | `/api/movies/{id}` | Update movie | 200 / 400 / 404 |
| DELETE | `/api/movies/{id}` | Delete movie | 204 / 404 |
| POST | `/api/shows` | Create show and seats | 201 |
| GET | `/api/shows` | List shows | 200 |
| GET | `/api/shows/{id}` | Get show | 200 / 404 |
| PUT | `/api/shows/{id}` | Update show | 200 / 400 |
| DELETE | `/api/shows/{id}` | Delete show | 204 / 400 |
| GET | `/api/shows/{id}/seats` | List seats and availability | 200 |
| POST | `/api/bookings` | Book a seat | 201 / 409 |
| GET | `/api/bookings` | List current user's bookings | 200 |
| GET | `/api/bookings/{id}` | Get own booking | 200 / 404 |
| DELETE | `/api/bookings/{id}` | Cancel own booking | 200 / 400 |

## Authentication examples

Signup:
```json
{"email":"student@example.com","password":"Passw0rd!"}
```

Signin uses the same body and returns:
```json
{"accessToken":"<signed-token>","tokenType":"Bearer","expiresIn":7200}
```

Booking request:
```json
{"showId":1,"seatNumber":"S01"}
```

Do not send an email in the booking body. The authenticated JWT subject is the booking owner.
