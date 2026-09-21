<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
require_once __DIR__ . '/../../lib/crypto.php';
require_once __DIR__ . '/../../lib/ai-service.php';
handle_options();

$user = current_user($pdo);
$stmt = $pdo->prepare('SELECT * FROM ai_settings WHERE user_id=? LIMIT 1');
$stmt->execute([(int)$user['id']]);
$s = $stmt->fetch();
if (!$s || empty($s['gemini_api_key_encrypted'])) json_response(['ok'=>false,'error'=>'GEMINI_KEY_NOT_CONFIGURED'],422);

try {
    $key = decrypt_secret($s['gemini_api_key_encrypted']);
    $result = generate_gemini_reply('Reply with exactly: E.D.I.T.H. connection test successful.', $key, $s['response_model'] ?: DEFAULT_RESPONSE_MODEL, $s['system_prompt'] ?: '');
    json_response(['ok'=>true,'reply'=>$result]);
} catch (Throwable $e) {
    json_response(['ok'=>false,'error'=>'AI_TEST_FAILED','message'=>$e->getMessage()],502);
}
