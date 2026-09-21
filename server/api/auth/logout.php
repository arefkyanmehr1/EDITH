<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/http.php';
require_once __DIR__ . '/../../lib/auth.php';
handle_options();

$token = bearer_token();
if ($token) {
    $stmt = $pdo->prepare('DELETE FROM sessions WHERE token_hash=?');
    $stmt->execute([hash('sha256',$token)]);
}
json_response(['ok'=>true]);
