<?php
declare(strict_types=1);
require_once __DIR__ . '/../config.php';
require_once __DIR__ . '/../lib/http.php';
require_once __DIR__ . '/../lib/crypto.php';
require_once __DIR__ . '/../lib/meta.php';
require_once __DIR__ . '/../lib/ai-service.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $mode = $_GET['hub_mode'] ?? $_GET['hub.mode'] ?? '';
    $token = $_GET['hub_verify_token'] ?? $_GET['hub.verify_token'] ?? '';
    $challenge = $_GET['hub_challenge'] ?? $_GET['hub.challenge'] ?? '';
    if ($mode === 'subscribe' && hash_equals(META_WEBHOOK_VERIFY_TOKEN, (string)$token)) {
        http_response_code(200);
        echo $challenge;
    } else {
        http_response_code(403);
        echo 'Invalid verify token';
    }
    exit;
}

handle_options();
if ($_SERVER['REQUEST_METHOD'] !== 'POST') json_response(['ok'=>false,'error'=>'METHOD_NOT_ALLOWED'],405);

$raw = file_get_contents('php://input') ?: '';
$data = json_decode($raw,true);
try {
    $stmt = $pdo->prepare('INSERT INTO webhook_events (payload,processed,received_at) VALUES (?,0,NOW())');
    $stmt->execute([$raw]);
    $eventId = (int)$pdo->lastInsertId();
} catch (Throwable $e) { $eventId = 0; }

if (!is_array($data)) json_response(['ok'=>true,'received'=>true]);

$entry = $data['entry'][0] ?? [];
$messaging = $entry['messaging'][0] ?? null;
if (!$messaging) {
    if ($eventId) $pdo->prepare('UPDATE webhook_events SET processed=1,processed_at=NOW() WHERE id=?')->execute([$eventId]);
    json_response(['ok'=>true,'ignored'=>true]);
}

$senderId = (string)($messaging['sender']['id'] ?? '');
$recipientId = (string)($messaging['recipient']['id'] ?? $entry['id'] ?? '');
$text = trim((string)($messaging['message']['text'] ?? ''));
if ($senderId === '' || $text === '') {
    json_response(['ok'=>true,'ignored'=>true]);
}

$stmt = $pdo->prepare('SELECT * FROM instagram_accounts WHERE instagram_user_id=? AND is_active=1 LIMIT 1');
$stmt->execute([$recipientId]);
$account = $stmt->fetch();
if (!$account && !empty($entry['id'])) {
    $stmt = $pdo->prepare('SELECT * FROM instagram_accounts WHERE instagram_user_id=? AND is_active=1 LIMIT 1');
    $stmt->execute([(string)$entry['id']]);
    $account = $stmt->fetch();
}
if (!$account) json_response(['ok'=>true,'ignored'=>true]);

$conv = $pdo->prepare('SELECT * FROM conversations WHERE instagram_account_id=? AND instagram_user_id=? LIMIT 1');
$conv->execute([(int)$account['id'],$senderId]);
$conversation = $conv->fetch();

if (!$conversation) {
    $stmt = $pdo->prepare('INSERT INTO conversations (user_id,instagram_account_id,instagram_user_id,status,lead_status,ai_enabled,last_message_at,created_at,updated_at) VALUES (?,?,?,"ai","new",1,NOW(),NOW(),NOW())');
    $stmt->execute([(int)$account['user_id'],(int)$account['id'],$senderId]);
    $conversationId = (int)$pdo->lastInsertId();
} else {
    $conversationId = (int)$conversation['id'];
    $pdo->prepare('UPDATE conversations SET last_message_at=NOW(),updated_at=NOW() WHERE id=?')->execute([$conversationId]);
}

$externalId = (string)($messaging['message']['mid'] ?? '');
$stmt = $pdo->prepare('INSERT INTO messages (conversation_id,sender_type,message_text,external_message_id,ai_generated,created_at) VALUES (?,"customer",?,?,0,NOW())');
$stmt->execute([$conversationId,$text,$externalId ?: null]);

$settingsStmt = $pdo->prepare('SELECT * FROM ai_settings WHERE user_id=? LIMIT 1');
$settingsStmt->execute([(int)$account['user_id']]);
$settings = $settingsStmt->fetch();

if ($settings && (int)$settings['ai_enabled'] === 1 && !empty($settings['gemini_api_key_encrypted'])) {
    try {
        $apiKey = decrypt_secret($settings['gemini_api_key_encrypted']);
        $reply = generate_gemini_reply($text,$apiKey,$settings['response_model'] ?: DEFAULT_RESPONSE_MODEL,$settings['system_prompt'] ?: '');
        $token = decrypt_secret($account['access_token_encrypted']);
        $sent = instagram_send_message((string)$account['instagram_user_id'],$senderId,$reply,$token);
        $sentId = (string)($sent['message_id'] ?? $sent['id'] ?? '');
        $save = $pdo->prepare('INSERT INTO messages (conversation_id,sender_type,message_text,external_message_id,ai_generated,ai_confidence,created_at) VALUES (?,"ai",?,?,?,?,NOW())');
        $save->execute([$conversationId,$reply,$sentId ?: null,1,98.0]);
    } catch (Throwable $e) {
        $pdo->prepare('INSERT INTO system_logs (user_id,type,action,input_data,output_data,status,created_at) VALUES (?,"ai_reply","webhook_ai_error",?,? ,"error",NOW())')
            ->execute([(int)$account['user_id'],$text,$e->getMessage()]);
    }
}

if ($eventId) $pdo->prepare('UPDATE webhook_events SET processed=1,processed_at=NOW() WHERE id=?')->execute([$eventId]);
json_response(['ok'=>true,'received'=>true]);
