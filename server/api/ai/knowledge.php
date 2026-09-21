<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$user = current_user($pdo);
$userId = (int)$user['id'];

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $stmt = $pdo->prepare('SELECT id,title,content,type,priority,is_active,created_at,updated_at FROM ai_knowledge WHERE user_id=? ORDER BY priority DESC,id DESC');
    $stmt->execute([$userId]);
    json_response(['ok'=>true,'items'=>$stmt->fetchAll()]);
}

$data = request_json();
$id = (int)($data['id'] ?? 0);
$title = trim((string)($data['title'] ?? ''));
$content = trim((string)($data['content'] ?? ''));
$type = trim((string)($data['type'] ?? 'permanent_instruction'));
$priority = (int)($data['priority'] ?? 0);
$active = !empty($data['is_active']) ? 1 : 0;

if ($title === '' || $content === '') json_response(['ok'=>false,'error'=>'INVALID_INPUT'],422);

if ($id > 0) {
    $stmt = $pdo->prepare('UPDATE ai_knowledge SET title=?,content=?,type=?,priority=?,is_active=?,updated_at=NOW() WHERE id=? AND user_id=?');
    $stmt->execute([$title,$content,$type,$priority,$active,$id,$userId]);
} else {
    $stmt = $pdo->prepare('INSERT INTO ai_knowledge (user_id,title,content,type,priority,is_active,created_at,updated_at) VALUES (?,?,?,?,?,?,NOW(),NOW())');
    $stmt->execute([$userId,$title,$content,$type,$priority,$active]);
    $id = (int)$pdo->lastInsertId();
}
json_response(['ok'=>true,'id'=>$id]);
