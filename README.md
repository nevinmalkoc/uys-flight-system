# UYS - Uçuş Yönetim Sistemi

Staj sürecimde geliştirdiğim, havalimanı operasyonlarını simüle eden olay güdümlü (event-driven) mikroservis tabanlı uçuş yönetim sistemi.

Sistemde canlı uçuş operasyonları ile geçmiş uçuşların arşivlenmesi birbirinden ayrılmış, servisler arası iletişim Kafka üzerinden asenkron event'lerle kurgulanmıştır.

---

## Mimari ve Modüller

Proje 4 ana servis ve 1 altyapı modülünden oluşuyor:

- **`flight-service`**: Uçuşların oluşturulduğu, güncellendiği ve durumlarının (boarding, departed, landed vb.) yönetildiği ana servis. Canlı operasyonel veriler MySQL üzerinde tutulur. WebSocket desteğiyle anlık durum değişikliklerini arayüze iletir.
- **`reference-manager`**: İstasyon, havalimanı ve havayolu referans verilerini yöneten servis. Veriler Redis üzerinde önbelleğe alınır, değişiklikler Kafka event'i olarak yayınlanır.
- **`flight_archive_service`**: Tamamlanan veya günü geçen uçuşları Kafka üzerinden dinleyip PostgreSQL (JSONB) arşiv veritabanına aktaran servis. Canlı operasyon veritabanını (OLTP) hafif tutmak için arşivleme (OLAP) ayrı bir veritabanında tutulmuştur.
- **`uys-frontend`**: Operasyon görevlilerinin uçuşları takip edebileceği, filtreleyebileceği ve yönetebileceği Vue.js arayüzü.
- **`uys-infra`**: Sistemin ihtiyaç duyduğu altyapı bileşenlerini (MySQL, PostgreSQL, Kafka, Zookeeper, Redis, Keycloak ve Nginx) içeren Docker Compose yapılandırması.

---

## Kullanılan Teknolojiler

- **Backend:** Java 17, Spring Boot, Spring Security (JWT & Keycloak entegrasyonu), WebSocket, Liquibase
- **Frontend:** Vue.js
- **Mesajlaşma & Event Streaming:** Apache Kafka, Zookeeper
- **Veritabanı & Cache:** MySQL 8 (Canlı operasyon - OLTP), PostgreSQL 15 (Arşivleme - OLAP JSONB), Redis 7 (Önbellek)
- **Container & Altyapı:** Docker, Docker Compose, Nginx

---

## Projeyi Yerelde Çalıştırma

### 1. Altyapıyı Başlatma
Veritabanları, Kafka, Redis ve Keycloak servislerini ayağa kaldırmak için:

```bash
cd uys-infra
docker compose up -d
```

### 2. Backend Servislerini Çalıştırma
Servisleri sırasıyla başlatabilirsiniz:
- `reference-manager`
- `flight-service`
- `flight_archive_service`

Her servisin kendi klasöründe:
```bash
./mvnw spring-boot:run
```

### 3. Frontend'i Çalıştırma
```bash
cd uys-frontend
npm install
npm run dev
```
