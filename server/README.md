# E.D.I.T.H. Backend

PHP/MySQL backend for the E.D.I.T.H. Android client.

## Production setup

Upload this directory to:

`public_html/instagram/`

Then:

1. Copy `config.example.php` to `config.php`.
2. Fill the MySQL credentials.
3. Generate a 64-character hexadecimal `APP_ENCRYPTION_KEY`.
4. Fill the Meta App ID/Secret and the exact OAuth callback URL configured in the Meta developer dashboard.
5. Import `schema.sql`.
6. Test `/api/health.php`.
7. Configure the Meta/Instagram webhook callback as `/api/webhook.php` and use the same verify token.
8. Keep `config.php` outside GitHub or ignored by Git.

## OAuth flow

The Android app authenticates to this backend first. It then requests `/api/instagram/connect.php`. The backend creates a one-time OAuth state and returns the Meta/Instagram authorization URL.

After consent, Meta calls `/api/instagram/callback.php`. The backend validates the state, exchanges the authorization code, obtains the access token, encrypts it with AES-256-GCM, associates it with the logged-in E.D.I.T.H. user, and redirects to the Android deep link.

The Instagram access token is never returned to the Android app.

## Webhook flow

Meta calls `/api/webhook.php`. The endpoint:

1. Verifies the webhook challenge.
2. Stores the raw event.
3. Maps the Instagram account to its E.D.I.T.H. owner.
4. Stores customer messages.
5. Loads that user's AI settings.
6. Generates a reply with the user's Gemini key.
7. Sends the reply through the Instagram API.
8. Stores the AI response.

## Security

Never commit:

- `config.php`
- Meta App Secret
- Gemini API keys
- Instagram access tokens
- database passwords

## Meta version/scopes

Meta changes API versions and permission names over time. The current values are intentionally configurable in `config.php`. Set them to the values shown by your Meta developer dashboard before production launch.
