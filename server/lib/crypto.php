<?php
declare(strict_types=1);

function encryption_key(): string {
    $hex = defined('APP_ENCRYPTION_KEY') ? APP_ENCRYPTION_KEY : '';
    $key = ctype_xdigit($hex) ? hex2bin($hex) : false;
    if ($key === false || strlen($key) !== 32) {
        throw new RuntimeException('APP_ENCRYPTION_KEY must be 64 hex characters.');
    }
    return $key;
}

function encrypt_secret(string $plaintext): string {
    $iv = random_bytes(12);
    $tag = '';
    $cipher = openssl_encrypt($plaintext, 'aes-256-gcm', encryption_key(), OPENSSL_RAW_DATA, $iv, $tag);
    if ($cipher === false) throw new RuntimeException('Encryption failed.');
    return base64_encode($iv . $tag . $cipher);
}

function decrypt_secret(string $encoded): string {
    $raw = base64_decode($encoded, true);
    if ($raw === false || strlen($raw) < 28) throw new RuntimeException('Invalid encrypted value.');
    $iv = substr($raw, 0, 12);
    $tag = substr($raw, 12, 16);
    $cipher = substr($raw, 28);
    $plain = openssl_decrypt($cipher, 'aes-256-gcm', encryption_key(), OPENSSL_RAW_DATA, $iv, $tag);
    if ($plain === false) throw new RuntimeException('Decryption failed.');
    return $plain;
}
