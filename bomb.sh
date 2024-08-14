for i in {1..1500};do
  curl  -vvv --header "Content-Type: application/json" --request POST \
  --data '{"side":"SELLER","quantity":"'"$(echo $RANDOM)"'","price":"'"$(echo $RANDOM)"'","currencyPair":"BTCZAR"}' \
  http://localhost:8888/v1/orders/limit;echo -e "\n";
  done
