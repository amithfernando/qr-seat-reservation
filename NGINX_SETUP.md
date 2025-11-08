# Nginx Configuration Guide

This project includes nginx as a reverse proxy for SSL/TLS termination and improved security.

## Overview

The docker-compose setup includes three services:
- **db**: MySQL 8 database
- **app**: QR Seat Reservation application (Java)
- **nginx**: Reverse proxy with SSL support

## SSL Certificate Setup

Before running the application with nginx, you need to provide SSL certificates. You have two options:

### Option 1: Self-Signed Certificates (Development/Testing)

Generate self-signed certificates using OpenSSL:

```bash
openssl req -x509 -newkey rsa:4096 -nodes \
  -keyout key.pem \
  -out cert.pem \
  -days 365 \
  -subj "/CN=localhost"
```

This creates:
- `cert.pem`: SSL certificate
- `key.pem`: Private key

### Option 2: Let's Encrypt (Production)

For production deployments, use Let's Encrypt certificates:

```bash
# Install certbot
sudo apt-get update
sudo apt-get install certbot

# Generate certificates (replace example.com with your domain)
sudo certbot certonly --standalone -d example.com

# Copy certificates to project directory
sudo cp /etc/letsencrypt/live/example.com/fullchain.pem cert.pem
sudo cp /etc/letsencrypt/live/example.com/privkey.pem key.pem
```

## Running the Application

1. Ensure you have SSL certificates (`cert.pem` and `key.pem`) in the project root directory

2. Start all services:
```bash
docker-compose up -d
```

3. Check service status:
```bash
docker-compose ps
```

4. View logs:
```bash
docker-compose logs -f
```

## Accessing the Application

- **HTTP**: http://localhost (redirects to HTTPS)
- **HTTPS**: https://localhost
- **Direct App Access** (bypassing nginx): http://localhost:8080

## Configuration Details

### MySQL Configuration
- Character set: utf8mb4
- Collation: utf8mb4_unicode_ci
- Health check with 40s start period and 10 retries
- Persistent data storage in `./app-db`

### Nginx Configuration
- HTTP (port 80): Redirects all traffic to HTTPS
- HTTPS (port 443): Terminates SSL and proxies to app:8080
- Security headers included
- WebSocket support enabled
- Max upload size: 10MB (configurable in nginx.conf)

### Application
- Runs on port 8080 (internal)
- Accessible via nginx on ports 80/443
- JVM options: -Xms256m -Xmx512m

## Customization

### Modifying Nginx Configuration

Edit `nginx.conf` to customize:
- Server name
- SSL protocols
- Proxy timeouts
- Upload size limits
- Additional security headers

After modifying, restart nginx:
```bash
docker-compose restart nginx
```

### Database Configuration

Database credentials can be changed in `docker-compose.yaml`:
- MYSQL_DATABASE
- MYSQL_USER
- MYSQL_PASSWORD
- MYSQL_ROOT_PASSWORD

Remember to update the `DB_URL`, `DB_USER`, and `DB_PWD` in the app service accordingly.

## Troubleshooting

### Certificate Errors
If you see SSL certificate errors in the browser when using self-signed certificates, this is expected. You can:
- Accept the security exception (development only)
- Use proper certificates from Let's Encrypt (production)

### Container Issues
```bash
# Check container logs
docker-compose logs app
docker-compose logs nginx
docker-compose logs db

# Restart specific service
docker-compose restart <service-name>

# Rebuild and restart
docker-compose up -d --build
```

### Database Connection Issues
If the app can't connect to the database:
1. Check if db container is healthy: `docker-compose ps`
2. Verify healthcheck is passing
3. Check db logs: `docker-compose logs db`

## Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes database data)
docker-compose down -v
```

## Security Considerations

1. **Production Deployments**:
   - Use Let's Encrypt or proper CA-signed certificates
   - Change default database passwords
   - Update `server_name` in nginx.conf to your domain
   - Consider using Docker secrets for sensitive data

2. **Firewall Configuration**:
   - Only expose ports 80 and 443 externally
   - Keep port 3306 (MySQL) and 8080 (app) internal

3. **Regular Updates**:
   - Keep Docker images updated
   - Renew SSL certificates before expiration
   - Apply security patches regularly
