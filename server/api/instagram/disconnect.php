<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$user = current_user($pdo);
$data = request_json();
$id = (int)($data['account_id'] ?? 0);
$stmt = $pdo->prepare('UPDATE instagram_accounts SET is_active=0,updated_at=NOW() WHERE id=? AND user_id=?');
$stmt->execute([$id,(int)$user['id']]);
json_response(['ok'=>true]);
