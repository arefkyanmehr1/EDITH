<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$data = request_json();
$email = strtolower(trim((string)($data['email'] ?? '')));
$password = (string)($data['password'] ?? '');
$name = trim((string)($data['name'] ?? ''));

if (!filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($password) < 8) {
    json_response(['ok'=>false,'error'=>'INVALID_INPUT'], 422);
}

$check = $pdo->prepare('SELECT id FROM users WHERE email=? LIMIT 1');
$check->execute([$email]);
if ($check->fetch()) json_response(['ok'=>false,'error'=>'EMAIL_EXISTS'], 409);

$stmt = $pdo->prepare('INSERT INTO users (name,email,password_hash,is_active,created_at,updated_at) VALUES (?,?,?,?,NOW(),NOW())');
$stmt->execute([$name ?: null, $email, password_hash($password, PASSWORD_DEFAULT), 1]);
$userId = (int)$pdo->lastInsertId();

$settings = $pdo->prepare('INSERT INTO ai_settings (user_id,response_model,analysis_model,ai_enabled) VALUES (?,?,?,1)');
$settings->execute([$userId, DEFAULT_RESPONSE_MODEL, DEFAULT_ANALYSIS_MODEL]);

$token = issue_session($pdo, $userId);
json_response(['ok'=>true,'token'=>$token,'user'=>['id'=>$userId,'name'=>$name,'email'=>$email]]);
