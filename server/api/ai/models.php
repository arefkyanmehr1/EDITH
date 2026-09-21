<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');

echo json_encode([
    'ok' => true,
    'response_model' => 'gemini-3.1-flash-lite',
    'analysis_model' => 'gemini-3.8-flash'
], JSON_UNESCAPED_UNICODE);
