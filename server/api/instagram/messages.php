<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$user = current_user($pdo);
$conversationId = (int)($_GET['conversation_id'] ?? 0);
if ($conversationId <= 0) json_response(['ok'=>false,'error'=>'INVALID_CONVERSATION'],422);

$check = $pdo->prepare('SELECT id FROM conversations WHERE id=? AND user_id=? LIMIT 1');
$check->execute([$conversationId,(int)$user['id']]);
if (!$check->fetch()) json_response(['ok'=>false,'error'=>'NOT_FOUND'],404);

$stmt = $pdo->prepare('SELECT id,sender_type,message_text,external_message_id,ai_generated,ai_confidence,created_at FROM messages WHERE conversation_id=? ORDER BY id ASC');
$stmt->execute([$conversationId]);
json_response(['ok'=>true,'messages'=>$stmt->fetchAll()]);
