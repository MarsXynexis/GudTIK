-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.4.3 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for gud_tik
CREATE DATABASE IF NOT EXISTS `gud_tik` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `gud_tik`;

-- Dumping structure for table gud_tik.barang
CREATE TABLE IF NOT EXISTS `barang` (
  `id_barang` varchar(50) NOT NULL,
  `nama_barang` varchar(255) NOT NULL,
  `kategori` enum('NETWORKING','COMPONENTS','PERIPHERALS','TOOLS','LAINNYA') NOT NULL,
  `tipe_barang` enum('CONSUMABLE','RETURNABLE') NOT NULL,
  `stok_tersedia` int NOT NULL DEFAULT '0',
  `sedang_dipinjam` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_barang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table gud_tik.barang: ~2 rows (approximately)

-- Dumping structure for table gud_tik.pemakaian
CREATE TABLE IF NOT EXISTS `pemakaian` (
  `id_pemakaian` varchar(50) NOT NULL,
  `id_barang` varchar(50) NOT NULL,
  `nim_pemakai` varchar(20) NOT NULL,
  `pelaku` varchar(100) NOT NULL,
  `jumlah` int NOT NULL,
  `tanggal_pakai` datetime DEFAULT CURRENT_TIMESTAMP,
  `tujuan_pemakaian` text,
  PRIMARY KEY (`id_pemakaian`),
  KEY `id_barang` (`id_barang`),
  CONSTRAINT `fk_pemakaian_barang` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table gud_tik.pemakaian: ~2 rows (approximately)

-- Dumping structure for table gud_tik.peminjaman
CREATE TABLE IF NOT EXISTS `peminjaman` (
  `id_peminjaman` varchar(50) NOT NULL,
  `id_barang` varchar(50) NOT NULL,
  `nim_peminjam` varchar(20) NOT NULL,
  `pelaku` varchar(100) NOT NULL,
  `jumlah` int NOT NULL,
  `tanggal_pinjam` datetime DEFAULT CURRENT_TIMESTAMP,
  `tenggat_waktu` datetime NOT NULL,
  `tanggal_kembali` datetime DEFAULT NULL,
  `status` enum('Dipinjam','Hilang','Rusak','Selesai') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'Dipinjam',
  PRIMARY KEY (`id_peminjaman`),
  KEY `id_barang` (`id_barang`),
  CONSTRAINT `fk_peminjaman_barang` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table gud_tik.peminjaman: ~4 rows (approximately)

-- Dumping structure for table gud_tik.riwayat_transaksi
CREATE TABLE IF NOT EXISTS `riwayat_transaksi` (
  `id_transaksi` varchar(50) NOT NULL,
  `id_barang` varchar(50) DEFAULT NULL,
  `id_peminjaman` varchar(50) DEFAULT NULL,
  `pelaku` varchar(100) NOT NULL,
  `jenis_transaksi` enum('Ditambahkan','Dipakai','Dipinjam','Dikembalikan','Rusak','Hilang','Dihapus') NOT NULL,
  `jumlah` int NOT NULL,
  `tanggal_transaksi` datetime DEFAULT CURRENT_TIMESTAMP,
  `keterangan` text,
  PRIMARY KEY (`id_transaksi`),
  KEY `id_barang` (`id_barang`),
  KEY `id_peminjaman` (`id_peminjaman`),
  CONSTRAINT `fk_riwayat_barang` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_riwayat_peminjaman` FOREIGN KEY (`id_peminjaman`) REFERENCES `peminjaman` (`id_peminjaman`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table gud_tik.riwayat_transaksi: ~15 rows (approximately)

-- Dumping structure for table gud_tik.users
CREATE TABLE IF NOT EXISTS `users` (
  `id_user` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) NOT NULL,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table gud_tik.users: ~1 rows (approximately)
INSERT INTO `users` (`id_user`, `username`, `password`, `nama_lengkap`) VALUES
	('U001', 'admin', 'admin123', 'Administrator Utama');

-- Dumping structure for trigger gud_tik.trg_after_insert_pemakaian
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `trg_after_insert_pemakaian` AFTER INSERT ON `pemakaian` FOR EACH ROW BEGIN
    INSERT INTO riwayat_transaksi (
        id_transaksi, 
        id_barang, 
        id_peminjaman, 
        pelaku, 
        jenis_transaksi, 
        jumlah, 
        keterangan
    ) VALUES (
        NEW.id_pemakaian, 
        NEW.id_barang, 
        NULL, -- Kosong karena ini bukan peminjaman
        NEW.pelaku, 
        'Dipakai', 
        NEW.jumlah, 
        CONCAT('Dipakai oleh NIM: ', NEW.nim_pemakai, ' - ', NEW.tujuan_pemakaian)
    );
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger gud_tik.trg_after_insert_peminjaman
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `trg_after_insert_peminjaman` AFTER INSERT ON `peminjaman` FOR EACH ROW BEGIN
    INSERT INTO riwayat_transaksi (
        id_transaksi, 
        id_barang, 
        id_peminjaman, 
        pelaku, 
        jenis_transaksi, 
        jumlah, 
        keterangan
    ) VALUES (
        NEW.id_peminjaman, 
        NEW.id_barang, 
        NEW.id_peminjaman, 
        NEW.pelaku, 
        'Dipinjam', 
        NEW.jumlah, 
        CONCAT('Dipinjam oleh NIM: ', NEW.nim_peminjam)
    );
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger gud_tik.trg_after_insert_riwayat
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `trg_after_insert_riwayat` AFTER INSERT ON `riwayat_transaksi` FOR EACH ROW BEGIN
    -- 1. Barang Baru Masuk Gudang
    IF NEW.jenis_transaksi = 'Ditambahkan' THEN
        UPDATE barang SET stok_tersedia = stok_tersedia + NEW.jumlah WHERE id_barang = NEW.id_barang;
        
    -- 2. Barang Dihapus/Dibuang dari Gudang
    ELSEIF NEW.jenis_transaksi = 'Dihapus' THEN
        UPDATE barang SET stok_tersedia = stok_tersedia - NEW.jumlah WHERE id_barang = NEW.id_barang;
        
    -- 3. Barang Dipinjam Mahasiswa
    ELSEIF NEW.jenis_transaksi = 'Dipinjam' THEN
        UPDATE barang SET stok_tersedia = stok_tersedia - NEW.jumlah, sedang_dipinjam = sedang_dipinjam + NEW.jumlah WHERE id_barang = NEW.id_barang;
        
    -- 4. Barang Selesai Dipinjam (Dikembalikan normal)
    ELSEIF NEW.jenis_transaksi = 'Dikembalikan' THEN
        UPDATE barang SET stok_tersedia = stok_tersedia + NEW.jumlah, sedang_dipinjam = sedang_dipinjam - NEW.jumlah WHERE id_barang = NEW.id_barang;
        
    -- 5. Barang Habis Pakai (Consumable)
    ELSEIF NEW.jenis_transaksi = 'Dipakai' THEN
        UPDATE barang SET stok_tersedia = stok_tersedia - NEW.jumlah WHERE id_barang = NEW.id_barang;
        
    -- 6. BARANG RUSAK ATAU HILANG (Logika Baru)
    ELSEIF NEW.jenis_transaksi = 'Rusak' OR NEW.jenis_transaksi = 'Hilang' THEN
        -- Hanya mengurangi sedang_dipinjam, TIDAK menambah stok_tersedia
        UPDATE barang SET sedang_dipinjam = sedang_dipinjam - NEW.jumlah WHERE id_barang = NEW.id_barang;
        
    END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
