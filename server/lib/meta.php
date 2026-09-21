<?php
declare(strict_types=1);

function meta_http(string $method, string $url, array $options = []): array {
    $ch = curl_init($url);
    $headers = $options['headers'] ?? [];
    $headers[] = 'Accept: application/json';
    if (!empty($options['json'])) {
        $headers[] = 'Content-Type: application/json';
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($options['json'], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES));
    }
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CUSTOMREQUEST => $method,
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_CONNECTTIMEOUT => 10,
    ]);
    $body = curl_exec($ch);
    $error = curl_error($ch);
    $status = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    $json = is_string($body) ? json_decode($body, true) : null;
    return ['status'=>$status, 'body'=>$body ?: '', 'json'=>is_array($json) ? $json : null, 'error'=>$error];
}

function instagram_oauth_url(string $state): string {
    $params = [
        'client_id' => META_APP_ID,
        'redirect_uri' => META_OAUTH_REDIRECT_URI,
        'response_type' => 'code',
        'scope' => META_SCOPES,
        'state' => $state,
    ];
    return META_OAUTH_AUTHORIZE_URL . '?' . http_build_query($params);
}

function exchange_instagram_code(string $code): array {
    $post = http_build_query([
        'client_id' => META_APP_ID,
        'client_secret' => META_APP_SECRET,
        'grant_type' => 'authorization_code',
        'redirect_uri' => META_OAUTH_REDIRECT_URI,
        'code' => $code,
    ]);
    $ch = curl_init(META_OAUTH_TOKEN_URL);
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => $post,
        CURLOPT_HTTPHEADER => ['Content-Type: application/x-www-form-urlencoded'],
        CURLOPT_TIMEOUT => 30,
    ]);
    $body = curl_exec($ch);
    $error = curl_error($ch);
    $status = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    $json = is_string($body) ? json_decode($body, true) : null;
    if ($status < 200 || $status >= 300 || !is_array($json) || empty($json['access_token'])) {
        throw new RuntimeException('Instagram token exchange failed: ' . ($error ?: ($body ?: 'unknown error')));
    }
    return $json;
}

function exchange_long_lived_token(string $shortToken): array {
    $url = META_LONG_LIVED_TOKEN_URL . '?' . http_build_query([
        'grant_type' => 'ig_exchange_token',
        'client_secret' => META_APP_SECRET,
        'access_token' => $shortToken,
    ]);
    $result = meta_http('GET', $url);
    if ($result['status'] < 200 || $result['status'] >= 300 || empty($result['json']['access_token'])) {
        throw new RuntimeException('Long-lived token exchange failed: ' . ($result['body'] ?: $result['error']));
    }
    return $result['json'];
}

function instagram_profile(string $accessToken): array {
    $url = META_GRAPH_BASE_URL . '/me?' . http_build_query([
        'fields' => 'id,user_id,username,name,profile_picture_url',
        'access_token' => $accessToken,
    ]);
    $result = meta_http('GET', $url);
    if ($result['status'] < 200 || $result['status'] >= 300 || empty($result['json'])) {
        throw new RuntimeException('Instagram profile request failed: ' . ($result['body'] ?: $result['error']));
    }
    return $result['json'];
}

function instagram_send_message(string $igUserId, string $recipientId, string $text, string $accessToken): array {
    $url = rtrim(META_GRAPH_BASE_URL, '/') . '/' . rawurlencode($igUserId) . '/messages';
    $result = meta_http('POST', $url, [
        'headers' => ['Authorization: Bearer ' . $accessToken],
        'json' => [
            'recipient' => ['id' => $recipientId],
            'message' => ['text' => $text],
        ],
    ]);
    if ($result['status'] < 200 || $result['status'] >= 300) {
        throw new RuntimeException('Instagram send message failed: ' . ($result['body'] ?: $result['error']));
    }
    return $result['json'] ?? [];
}
