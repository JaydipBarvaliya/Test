mkdir -p /etc/credentials


vi /etc/credentials/d2-tdbfg.nasuserpass

username=TDGVLM942NASB
password=<password>
domain=d2-tdbfg


chmod 600 /etc/credentials/d2-tdbfg.nasuserpass


mount -t cifs //NSAPDVCS01.D2-TDBFG.COM/SRCHD_0027D /mnt/dgvlm-nas-dna \
-o credentials=/etc/credentials/d2-tdbfg.nasuserpass

vi /etc/salt/grains

cifs:
  credential: nasuserpass
  device: //NSAPDVCS01.D2-TDBFG.COM/SRCHD_0027D
  name: /mnt/dgvlm-nas-dna
  groupname: springboot
  dirmode: 0775
  filemode: 0640

systemctl restart salt-minion

salt-call state.sls cifs

