-- ====================================================================
-- FILE INSTALASI DATABASE ABSENSI DAN PENGINGAT WHATSAPP GURU
-- Versi: 1.0.0
-- Password Database Akses: Sukses88
-- Kompatibel: MySQL 5.7+ / MariaDB 10.3+ / SQLite 3
-- Dibuat untuk: Absen Pulang Guru Otomatis via Fonnte Gateway WhatsApp
-- ====================================================================

-- 1. Tabel Konfigurasi Aplikasi & Token Fonnte
CREATE TABLE IF NOT EXISTS `tb_konfigurasi` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `kunci` VARCHAR(100) NOT NULL UNIQUE,
    `nilai` TEXT NOT NULL,
    `keterangan` VARCHAR(255) DEFAULT NULL,
    `diperbarui_pada` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inisialisasi Konfigurasi Bawaan
INSERT OR REPLACE INTO `tb_konfigurasi` (`kunci`, `nilai`, `keterangan`) VALUES
('fonnte_token', 'DEc5ECUwTKXBpnh989tD', 'Token API Fonnte WhatsApp Gateway'),
('database_password', 'Sukses88', 'Kata sandi keamanan manajemen database'),
('nama_sekolah', 'SLB N 1 KOTA GAJAH', 'Nama Institusi / Sekolah'),
('link_absen_pulang', 'https://absensi.sekolah.id/pulang', 'Tautan Web Absensi Pulang Guru'),
('notifikasi_aktif', 'true', 'Status aktifasi background scheduler');

-- 2. Tabel Data Guru / Tenaga Pendidik
CREATE TABLE IF NOT EXISTS `tb_guru` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `nip` VARCHAR(50) DEFAULT NULL,
    `nama` VARCHAR(150) NOT NULL,
    `nomor_whatsapp` VARCHAR(25) NOT NULL,
    `jabatan` VARCHAR(100) DEFAULT 'Guru',
    `status_aktif` INTEGER DEFAULT 1,
    `dibuat_pada` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Data Sampel Guru
INSERT INTO `tb_guru` (`nip`, `nama`, `nomor_whatsapp`, `jabatan`, `status_aktif`) VALUES
('197505102005011002', 'Drs. Supriyanto, M.Pd.', '6281234567890', 'Kepala Sekolah', 1),
('198208152009022003', 'Siti Rahmawati, S.Pd.', '6285712345678', 'Waka Kurikulum', 1),
('198603212011011004', 'Ahmad Fauzi, S.Pd.', '6287812345678', 'Guru Kelas', 1),
('199011122019032005', 'Nurul Hidayah, S.Kom.', '6289612345678', 'Guru TIK & Operator', 1),
('199307042022041006', 'Budi Santoso, S.Pd.', '6282112345678', 'Guru Penjaskes', 1);

-- 3. Tabel Jadwal Pengingat WhatsApp
CREATE TABLE IF NOT EXISTS `tb_jadwal` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `judul` VARCHAR(150) NOT NULL,
    `target_tipe` VARCHAR(50) NOT NULL, -- 'SEMUA_GURU', 'KONTAK_PILIHAN', 'GRUP_WA'
    `target_nilai` TEXT DEFAULT NULL,   -- nomor WA atau ID Grup (contoh: 1203630248849@g.us)
    `hari_aktif` VARCHAR(100) NOT NULL, -- Comma-separated: 'Senin,Selasa,Rabu,Kamis,Jumat'
    `jam_kirim` INTEGER NOT NULL,       -- Jam (0-23), misal: 15
    `menit_kirim` INTEGER NOT NULL,     -- Menit (0-59), misal: 0
    `template_pesan` TEXT NOT NULL,
    `status_aktif` INTEGER DEFAULT 1,
    `dibuat_pada` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Data Sampel Jadwal Absen Pulang
INSERT INTO `tb_jadwal` (`judul`, `target_tipe`, `target_nilai`, `hari_aktif`, `jam_kirim`, `menit_kirim`, `template_pesan`, `status_aktif`) VALUES
(
    'Pengingat Absen Pulang Guru (Senin-Kamis)',
    'SEMUA_GURU',
    '',
    'Senin,Selasa,Rabu,Kamis',
    15,
    0,
    '🔔 *PENGINGAT ABSEN PULANG GURU*\n\nKepada Yth. Bapak/Ibu *{nama_guru}*,\n\nWaktu kepulangan sekolah telah tiba pada pukul *{jam}* WIB.\nMohon untuk tidak lupa melakukan *Presensi/Absen Pulang* hari ini tanggal *{tanggal}*.\n\n📱 Link Presensi: {link_absen}\n\nTerima kasih atas dedikasi dan pengabdian Bapak/Ibu hari ini. Selamat beristirahat dan semoga selamat sampai di rumah. 🙏✨\n\n_Pesan otomatis dikirim oleh Sistem Presensi SLB N 1 Kota Gajah_',
    1
),
(
    'Pengingat Absen Pulang Hari Jumat',
    'SEMUA_GURU',
    '',
    'Jumat',
    11,
    30,
    '🔔 *PENGINGAT ABSEN PULANG HARI JUMAT*\n\nKepada Yth. Bapak/Ibu *{nama_guru}*,\n\nMengingatkan jam dinas hari Jumat telah selesai pada pukul *{jam}* WIB. Harap segera mengisi absen pulang:\n\n📱 Link: {link_absen}\n\nSelamat berakhir pekan bersama keluarga tercinta! 🙏✨',
    1
);

-- 4. Tabel Riwayat & Presensi Guru
CREATE TABLE IF NOT EXISTS `tb_presensi` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `guru_id` INTEGER NOT NULL,
    `tanggal` DATE NOT NULL,
    `jam_pulang` TIME DEFAULT NULL,
    `status_absen` VARCHAR(50) DEFAULT 'BELUM', -- 'HADIR', 'PULANG', 'IZIN', 'SAKIT', 'BELUM'
    `catatan` TEXT DEFAULT NULL,
    FOREIGN KEY (`guru_id`) REFERENCES `tb_guru`(`id`) ON DELETE CASCADE
);

-- 5. Tabel Log Riwayat Pengiriman WhatsApp Fonnte
CREATE TABLE IF NOT EXISTS `tb_log_kirim` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `jadwal_id` INTEGER DEFAULT NULL,
    `target_tujuan` VARCHAR(150) NOT NULL,
    `isi_pesan` TEXT NOT NULL,
    `status` VARCHAR(50) NOT NULL, -- 'SUKSES', 'GAGAL', 'PROSES'
    `response_api` TEXT DEFAULT NULL,
    `waktu_kirim` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Selesai Instalasi Database Absen Guru.
-- Password Akses: Sukses88
