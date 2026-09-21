<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$user = current_user($pdo);
$stmt = $pdo->prepare('SELECT id,instagram_user_id,username,name,profile_picture_url,is_active,token_expires_at,created_at,updated_at FROM instagram_accounts WHERE user_id=? AND is_active=1 ORDER BY id DESC');
$stmt->execute([(int)$user['id']]);
json_response(['ok'=>true,'accounts'=>$stmt->fetchAll()]);
