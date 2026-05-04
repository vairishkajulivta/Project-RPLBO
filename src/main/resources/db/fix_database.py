"""
Script perbaikan sahabatlaris.db
Jalankan: python fix_database.py
Letakkan file ini di folder yang sama dengan sahabatlaris.db
"""

import os
import shutil

DB_FILE = "sahabatlaris.db"
BACKUP_FILE = "sahabatlaris_backup.db"

def fix_db():
    if not os.path.exists(DB_FILE):
        print(f"ERROR: File {DB_FILE} tidak ditemukan!")
        print(f"Pastikan script ini ada di folder yang sama dengan {DB_FILE}")
        return

    # Backup dulu
    shutil.copy2(DB_FILE, BACKUP_FILE)
    print(f"Backup dibuat: {BACKUP_FILE}")

    lines = []
    with open(DB_FILE, "r", encoding="utf-8") as f:
        lines = f.readlines()

    # Bersihkan \r (CRLF -> LF)
    lines = [l.replace('\r\n', '\n').replace('\r', '\n') for l in lines]

    section = ""
    new_lines = []
    fixed_count = 0

    for line in lines:
        stripped = line.strip()
        if stripped.startswith("[") and stripped.endswith("]"):
            section = stripped[1:-1]
            new_lines.append(line)
            continue

        if section == "PRODUK" and stripped:
            cols = stripped.split("\t")
            # Format yg benar: kode|nama|kat|harga|kandungan|aktif|jenisKulit|areaTubuh|gambarUrl
            # cols[0..8] = 9 kolom
            if len(cols) >= 8:
                kode      = cols[0]
                nama      = cols[1]
                kat       = cols[2]
                harga     = cols[3]
                kandungan = cols[4]
                aktif     = cols[5]
                jenisKulit = cols[6]

                # Cek apakah cols[7] berisi path gambar (bug lama) atau areaTubuh
                col7 = cols[7] if len(cols) > 7 else ""
                col8 = cols[8] if len(cols) > 8 else ""

                is_image_path = ("images/" in col7 or col7.endswith((".png",".jpg",".jpeg",".gif",".webp")))

                if is_image_path:
                    # Bug: cols[7] = gambarUrl, areaTubuh hilang → fix
                    areaTubuh = "Muka"
                    gambarUrl = col7
                    fixed_count += 1
                    print(f"  FIX [{kode}] {nama}: gambar='{gambarUrl}'")
                else:
                    areaTubuh = col7 if col7 else "Muka"
                    gambarUrl = col8

                new_line = "\t".join([kode, nama, kat, harga, kandungan, aktif,
                                      jenisKulit, areaTubuh, gambarUrl]) + "\n"
                new_lines.append(new_line)
                continue

        new_lines.append(line)

    with open(DB_FILE, "w", encoding="utf-8") as f:
        f.writelines(new_lines)

    print(f"\nSelesai! {fixed_count} produk diperbaiki.")
    print(f"Database disimpan: {DB_FILE}")

if __name__ == "__main__":
    fix_db()
    input("\nTekan Enter untuk keluar...")
