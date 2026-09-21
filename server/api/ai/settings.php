<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
require_once __DIR__ . '/../../lib/crypto.php';
handle_options();

$user = current_user($pdo);
$userId = (int)$user['id'];

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $stmt = $pdo->prepare('SELECT response_model,analysis_model,system_prompt,temperature,max_output_tokens,ai_enabled,gemini_api_key_encrypted FROM ai_settings WHERE user_id=? LIMIT 1');
    $stmt->execute([$userId]);
    $s = $stmt->fetch() ?: [];
    json_response(['ok'=>true,'settings'=>[
        'response_model'=>$s['response_model'] ?? DEFAULT_RESPONSE_MODEL,
        'analysis_model'=>$s['analysis_model'] ?? DEFAULT_ANALYSIS_MODEL,
        'system_prompt'=>$s['system_prompt'] ?? '',
        'temperature'=>(float)($s['temperature'] ?? 0.4),
        'max_output_tokens'=>(int)($s['max_output_tokens'] ?? 512),
        'ai_enabled'=>(bool)($s['ai_enabled'] ?? true),
        'has_gemini_api_key'=>!empty($s['gemini_api_key_encrypted'])
    ]]);
}

$data = request_json();
$apiKey = trim((string)($data['gemini_api_key'] ?? ''));
$encrypted = null;
if ($apiKey !== '') $encrypted = encrypt_secret($apiKey);

$stmt = $pdo->prepare(
    'INSERT INTO ai_settings (user_id,gemini_api_key_encrypted,response_model,analysis_model,system_prompt,temperature,max_output_tokens,ai_enabled,updated_at)
     VALUES (?,?,?,?,?,?,?,?,NOW())
     ON DUPLICATE KEY UPDATE
     gemini_api_key_encrypted=COALESCE(VALUES(gemini_api_key_encrypted),gemini_api_key_encrypted),
     response_model=VALUES(response_model),analysis_model=VALUES(analysis_model),system_prompt=VALUES(system_prompt),
     temperature=VALUES(temperature),max_output_tokens=VALUES(max_output_tokens),ai_enabled=VALUES(ai_enabled),updated_at=NOW()'
);
$stmt->execute([
    $userId,
    $encrypted,
    trim((string)($data['response_model'] ?? DEFAULT_RESPONSE_MODEL)),
    trim((string)($data['analysis_model'] ?? DEFAULT_ANALYSIS_MODEL)),
    (string)($data['system_prompt'] ?? ''),
    max(0.0,min(1.0,(float)($data['temperature'] ?? 0.4))),
    max(64,min(4096,(int)($data['max_output_tokens'] ?? 512))),
    !empty($data['ai_enabled']) ? 1 : 0
]);
json_response(['ok'=>true]);
