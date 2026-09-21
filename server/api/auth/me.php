<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$user = current_user($pdo);
unset($user['password_hash']);
json_response(['ok'=>true,'user'=>$user]);
