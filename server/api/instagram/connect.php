<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
require_once __DIR__ . '/../../lib/meta.php';
handle_options();

$user = current_user($pdo);
$state = rtrim(strtr(base64_encode(random_bytes(32)), '+/', '-_'), '=');
$stmt = $pdo->prepare('INSERT INTO oauth_states (user_id,state,expires_at,created_at) VALUES (?,?,DATE_ADD(NOW(),INTERVAL 10 MINUTE),NOW())');
$stmt->execute([(int)$user['id'], $state]);

json_response(['ok'=>true,'auth_url'=>instagram_oauth_url($state)]);
