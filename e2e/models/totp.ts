import crypto from 'node:crypto'

/** Calcule un code TOTP (RFC 6238) à partir d'un secret base32 — pour les tests MFA. */
export function genererTotp(secretBase32: string): string {
  const key = decodeBase32(secretBase32)
  const compteur = Math.floor(Date.now() / 1000 / 30)
  const buf = Buffer.alloc(8)
  buf.writeBigInt64BE(BigInt(compteur))
  const hmac = crypto.createHmac('sha1', key).update(buf).digest()
  const offset = hmac[hmac.length - 1] & 0xf
  const bin = ((hmac[offset] & 0x7f) << 24) | ((hmac[offset + 1] & 0xff) << 16)
    | ((hmac[offset + 2] & 0xff) << 8) | (hmac[offset + 3] & 0xff)
  return String(bin % 1_000_000).padStart(6, '0')
}

function decodeBase32(s: string): Buffer {
  const base32 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'
  const bytes: number[] = []
  let bits = 0, value = 0
  for (const c of s) {
    value = (value << 5) | base32.indexOf(c)
    bits += 5
    if (bits >= 8) { bytes.push((value >>> (bits - 8)) & 0xff); bits -= 8 }
  }
  return Buffer.from(bytes)
}
