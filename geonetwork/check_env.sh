if [ -z "$1" -o "$1" == "null" ]; then 
  echo "[ERROR] '-Dserver param is required'"
  exit -1  
fi