<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$user = current_user($pdo);
$stmt = $pdo->prepare('SELECT id,instagram_account_id,instagram_user_id,username,status,lead_status,ai_enabled,last_message_at,created_at,updated_at FROM conversations WHERE user_id=? ORDER BY COALESCE(last_message_at,created_at) DESC');
$stmt->execute([(int)$user['id']]);
json_response(['ok'=>true,'conversations'=>$stmt->fetchAll()]);
