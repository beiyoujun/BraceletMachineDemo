# -*- coding: UTF-8 -*-
from botocore.exceptions import ClientError
import os
import zipfile
import shutil
import boto3
import requests
import json
import sys
import time

root = 'E:/Zjy/Android Projects/SmartReceptionSDKDemo/SmartReceptionSDKDemo/'
kotlinProject = root+'app'
javaProject = root+'smartreceptionsdkdemojava'
kotlinPath = kotlinProject+'/build/'
javaPath = javaProject+'/build'
compressPath = root+'BraceletMachineDemo'

zipPath = "E:\Zjy\Android Projects\SmartReceptionSDKDemo\SmartReceptionSDKDemo\BraceletMachineDemo.zip"
rarPath = "E:\Zjy\Android Projects\SmartReceptionSDKDemo\SmartReceptionSDKDemo\BraceletMachineDemo.rar"

def del_path(path):
    if os.path.exists(path):
        if os.path.isfile(path):
            os.remove(path)
        else:
            items = os.listdir(path)
            for f in items:
                c_path = os.path.join(path, f)
                if os.path.isdir(c_path):
                    del_path(c_path)
                else:
                    os.remove(c_path)
            os.rmdir(path)

def mycopy(srcpath,dstpath,arc):
    start = len(srcpath)
    if not os.path.exists(srcpath):
        print "srcpath not exist!"
    if not os.path.exists(dstpath):
        os.mkdir(dstpath)
    for root,dirs,files in os.walk(srcpath,True):
        for eachfile in files:
            arcfilename = arc+root[start:].replace('\\','/')
            dstDir = dstpath+arcfilename
            copyTo = dstDir+"/"+eachfile
            if not os.path.exists(dstDir):
                os.makedirs(dstDir,0755)
            shutil.copy(os.path.join(root,eachfile),copyTo)

class FileObj:
    def __init__(self, path, filename,arc):
        self.path = path
        self.filename = filename
        self.arc = arc
        self.fullpath = path+filename

def zip_dir(dirname,zipfilename):
  filelist = []
  if os.path.isfile(dirname):
    filelist.append(dirname)
  else :
    for rt, dirs, files in os.walk(dirname):
      for name in files:
        filelist.append(os.path.join(rt, name))
  zf = zipfile.ZipFile(zipfilename, "w", zipfile.zlib.DEFLATED)
  for tar in filelist:
    arcname = 'BraceletMachineDemo\\'+tar[len(dirname):]
    zf.write(tar,arcname)
  zf.close()

s3 = boto3.client(
    's3',
    region_name='cn-north-1',
    aws_access_key_id='AKIAOKNINXYZBKLV5BPQ',
    aws_secret_access_key='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'
)

def upload_file(file_name, object_name=None):
    """Upload a file to an S3 bucket

    :param file_name: File to upload
    :param bucket: Bucket to upload to
    :param object_name: S3 object name. If not specified then file_name is used
    :return: True if file was uploaded, else False
    """
    # If S3 object_name was not specified, use file_name
    if object_name is None:
        object_name = 'apk/BraceletMachineDemo.rar'
        
    try:
        try:
            file_name.index(".css")
            with open(file_name) as f:
                response = s3.put_object(Body=f, Bucket='ocmshare',ContentType='text/css',Key=object_name)
        except ValueError as e: 
            response = s3.upload_file(file_name, 'ocmshare', object_name)
    except ClientError as e:
        logging.error(e)
        return False
    return True

def get_aar_path(srcpath):
    for root,dirs,files in os.walk(srcpath):
        for eachfile in files:
            if(eachfile.find(".aar")!=-1):
                print(eachfile)
                return eachfile
    return False


def update_version(version):
    url = 'http://api.cqzhaoquan.com/update_sdk_version'
    payload = { "type":"1","version":version}
    response = requests.post(url=url,data=payload)
    print(response.text)
    
aarPath = get_aar_path(root)
header = "bracelet_machine_sdk-release-"
start = len(header)
end = aarPath.find(".aar")
sdk_version = aarPath[start:end]


# update_version("1.0.10")

if(aarPath==False):
    print(unicode("确实arr文件，请将.aar放置于当前目录", encoding="utf-8"))
else:
    del_path(compressPath)
    del_path(kotlinPath)
    del_path(javaPath)
    mycopy(kotlinProject,compressPath,"/app")
    mycopy(javaProject,compressPath,"/smartreceptionsdkdemojava")
    shutil.copyfile(os.path.join(root,aarPath),compressPath+"/"+aarPath)
    zip_dir(compressPath,root+"BraceletMachineDemo.rar")
    upload_file(rarPath)
    update_version(sdk_version)
    print "Upload Success!"