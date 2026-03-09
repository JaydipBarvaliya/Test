-- Insert default configuration data

INSERT INTO STOR_CONFIG (
    ID,
    LOB_ID,
    STOR_SYS,
    REPO_ID,
    FOLDER_PATH,
    NAS_LOCATION
) VALUES (
    STOR_CONFIG_SEQ.NEXTVAL,
    'dna',
    'FileNet',
    'CS_FBD_LSWOS1',
    '/mnt/dgvlm-nas-dna/BatchDoc_DNA',
    '\\NSAPDVCS01.D2-TDBFG.COM\SRCHD_0027D'
);

INSERT INTO STOR_CONFIG (
    ID,
    LOB_ID,
    STOR_SYS,
    REPO_ID,
    FOLDER_PATH,
    NAS_LOCATION
) VALUES (
    STOR_CONFIG_SEQ.NEXTVAL,
    'tdiclaims',
    'FileNet',
    'DGVLM_GWCC_TDI',
    '/TDI-FileNet-Documents/BATCHDOC',
    '\\NSAPDVCS03.D2-TDBFG.COM\ICDMS_007F2'
);

COMMIT;