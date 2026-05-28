// Generate self-signed certificates for local development
const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');

const keyPath = path.join(__dirname, 'localhost.key');
const certPath = path.join(__dirname, 'localhost.crt');

// Check if certs already exist
if (fs.existsSync(keyPath) && fs.existsSync(certPath)) {
  console.log('✓ SSL certificates already exist');
  process.exit(0);
}

console.log('Attempting to generate SSL certificates...');

// Try using Node's crypto module as fallback
try {
  const forge = require('node-forge');
  console.log('Using node-forge to generate certificates...');

  // Generate key pair
  const keys = forge.pki.rsa.generateKeyPair(2048);

  // Create certificate
  const cert = forge.pki.createCertificate();
  cert.publicKey = keys.publicKey;
  cert.serialNumber = '01';
  cert.validity.notBefore = new Date();
  cert.validity.notAfter = new Date();
  cert.validity.notAfter.setFullYear(cert.validity.notAfter.getFullYear() + 1);
  cert.setSubject([{ name: 'commonName', value: 'localhost' }]);
  cert.setIssuer([{ name: 'commonName', value: 'localhost' }]);
  cert.setExtensions([
    { name: 'basicConstraints', cA: false },
    { name: 'keyUsage', keyCertSign: false, digitalSignature: true, nonRepudiation: true, keyEncipherment: true, dataEncipherment: true }
  ]);
  cert.sign(keys.privateKey, forge.md.sha256.create());

  // Write to files
  const pem = forge.pki.certificateToPem(cert);
  const key = forge.pki.privateKeyToPem(keys.privateKey);

  fs.writeFileSync(certPath, pem);
  fs.writeFileSync(keyPath, key);
  console.log('✓ Generated localhost.crt and localhost.key using node-forge');
} catch (e) {
  console.error('Failed to generate certificates:', e.message);
  console.error('Please install OpenSSL and generate certificates manually:');
  console.error('  openssl req -x509 -newkey rsa:2048 -keyout localhost.key -out localhost.crt -days 365 -nodes -subj "/CN=localhost"');
  process.exit(1);
}

