<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
require_once __DIR__ . '/../../lib/crypto.php';
require_once __DIR__ . '/../../lib/meta.php';
handle_options();

$user = current_user($pdo);
$stmt = $pdo->prepare('SELECT * FROM instagram_accounts WHERE user_id=? AND is_active=1 ORDER BY id DESC LIMIT 1');
$stmt->execute([(int)$user['id']]);
$account = $stmt->fetch();
if (!$account) json_response(['ok'=>false,'error'=>'INSTAGRAM_NOT_CONNECTED'],404);

try {
    $token = decrypt_secret($account['access_token_encrypted']);
    $url = rtrim(META_GRAPH_BASE_URL,'/') . '/me/media?' . http_build_query([
        'fields'=>'id,caption,media_type,media_url,thumbnail_url,permalink,timestamp',
        'limit'=>25,
        'access_token'=>$token
    ]);
    $result = meta_http('GET',$url);
    if ($result['status'] < 200 || $result['status'] >= 300) {
        json_response(['ok'=>false,'error'=>'INSTAGRAM_API_ERROR','details'=>$result['body']],502);
    }
    json_response(['ok'=>true,'posts'=>$result['json']['data'] ?? []]);
} catch (Throwable $e) {
    json_response(['ok'=>false,'error'=>'POST_FETCH_FAILED','message'=>$e->getMessage()],502);
}
