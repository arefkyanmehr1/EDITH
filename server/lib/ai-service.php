<?php
declare(strict_types=1);

function generate_gemini_reply(string $message, string $apiKey, string $model, string $systemPrompt = ''): string {
    $model = trim($model);
    $url = 'https://generativelanguage.googleapis.com/v1beta/models/' . rawurlencode($model) . ':generateContent?key=' . rawurlencode($apiKey);
    $contents = [];
    if ($systemPrompt !== '') {
        $contents[] = ['role'=>'user','parts'=>[['text'=>$systemPrompt]]];
        $contents[] = ['role'=>'model','parts'=>[['text'=>'Understood.']]];
    }
    $contents[] = ['role'=>'user','parts'=>[['text'=>$message]]];

    $payload = json_encode(['contents'=>$contents], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER=>true,
        CURLOPT_POST=>true,
        CURLOPT_POSTFIELDS=>$payload,
        CURLOPT_HTTPHEADER=>['Content-Type: application/json'],
        CURLOPT_TIMEOUT=>45
    ]);
    $body = curl_exec($ch);
    $error = curl_error($ch);
    $status = (int)curl_getinfo($ch,CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($status < 200 || $status >= 300) throw new RuntimeException('Gemini HTTP '.$status.': '.($body ?: $error));
    $json = json_decode((string)$body,true);
    $text = $json['candidates'][0]['content']['parts'][0]['text'] ?? '';
    if ($text === '') throw new RuntimeException('Gemini returned no text.');
    return trim($text);
}
