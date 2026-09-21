<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ '/../../lib/auth.php';
handle_options();

$data = request_json();
$email = strtolower(trim((string)($data['email'] ?? '')));
$password = (string)($data['password'] ?? '');

$stmt = $pdo->prepare('SELECT id,name,email,password_hash FROM users WHERE email=? AND is_active=1 LIMIT 1');
$stmt->execute([$email]);
$user = $stmt->fetch();
if (!$user || !password_verify($password, $user['password_hash'])) {
    json_response(['ok'=>false,'error'=>'INVALID_CREDENTIALS'], 401);
}

$token = issue_session($pdo, (int)$user['id']);
json_response(['ok'=>true,'token'=>$token,'user'=>['id'=>(int)$user['id'],'name'=>$user['name'],'email'=>$user['email']]]);
