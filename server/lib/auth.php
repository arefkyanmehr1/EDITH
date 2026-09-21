<?php
declare(strict_types=1);

require_once __DIR__ . '/crypto.php';

function issue_session(PDO $pdo, int $userId): string {
    $plain = rtrim(strtr(base64_encode(random_bytes(48)), '+/', '-_'), '=');
    $hash = hash('sha256', $plain);
    $stmt = $pdo->prepare('INSERT INTO sessions (user_id, token_hash, expires_at, created_at) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW())');
    $stmt->execute([$userId, $hash]);
    return $plain;
}

function current_user(PDO $pdo): array {
    $token = bearer_token();
    if (!$token) json_response(['ok' => false, 'error' => 'AUTH_REQUIRED'], 401);

    $hash = hash('sha256', $token);
    $stmt = $pdo->prepare('SELECT u.* FROM sessions s JOIN users u ON u.id=s.user_id WHERE s.token_hash=? AND s.expires_at>NOW() AND u.is_active=1 LIMIT 1');
    $stmt->execute([$hash]);
    $user = $stmt->fetch();
    if (!$user) json_response(['ok' => false, 'error' => 'INVALID_SESSION'], 401);
    return $user;
}
