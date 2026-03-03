sudo mkdir -p /mnt/dgvlm-dna && \
sudo chown springboot:springboot /mnt/dgvlm-dna && \
sudo chmod 775 /mnt/dgvlm-dna && \
sudo mount -t cifs //NSAPDVCS01.D2-TDBFG.COM/SRCHD_0027D /mnt/dgvlm-dna \
-o username=TDGVLM942NASB,password="1k^\eCCeV;o5luF0ik94<9:Uk",domain=D2-TDBFG,vers=3.0