string = """borrow Elon Annabelle 183.64         
repay Carlos Billibob 182.41         
repay Billibob Annabelle 86.66       
borrow Diana Elon 194.80             
repay Carlos Diana 123.11            
borrow Finny Elon 186.92             
repay Diana Billibob 76.72           
repay Elon Diana 152.30              
borrow Billibob Diana 168.85         
borrow Diana Billibob 46.57          
borrow Diana Annabelle 17.46         
borrow Diana Annabelle 95.87         
borrow Diana Billibob 96.57          
repay Carlos Annabelle 13.88         
repay Diana Carlos 67.91             
repay Finny Elon 40.77               
repay Elon Finny 188.35              
repay Elon Diana 27.82               
repay Annabelle Elon 75.56           
repay Diana Finny 29.7               
borrow Carlos Annabelle 169.63       
borrow Carlos Finny 155.23           
borrow Diana Finny 47.66             
repay Elon Carlos 56.71              
repay Billibob Finny 116.29          
borrow Annabelle Finny 42.7          
repay Finny Billibob 131.15          
borrow Annabelle Carlos 114.41       
borrow Diana Finny 194.52            
repay Diana Finny 7.56               
borrow Finny Billibob 84.70          
repay Carlos Finny 130.4             
borrow Diana Carlos 153.30           
borrow Billibob Finny 49.30          
borrow Elon Diana 50.25              
borrow Billibob Annabelle 90.21      
repay Billibob Elon 110.64           
borrow Carlos Finny 159.2            
borrow Diana Elon 111.22             
borrow Billibob Carlos 23.20         
repay Diana Carlos 187.96            
repay Elon Diana 47.39               
repay Elon Carlos 151.54             
repay Carlos Diana 167.65            
borrow Finny Annabelle 43.12         
repay Diana Annabelle 127.7          
borrow Diana Finny 62.71             
borrow Finny Carlos 192.56           
borrow Annabelle Billibob 115.41     
borrow Finny Carlos 26.66            
borrow Annabelle Finny 16.43         
repay Annabelle Carlos 58.35         
repay Finny Diana 76.42              
borrow Billibob Finny 43.82          
borrow Elon Annabelle 106.17         
borrow Billibob Diana 94.89          
borrow Diana Annabelle 48.33         
borrow Annabelle Finny 16.47         
repay Carlos Finny 186.5             
borrow Billibob Finny 38.79          
repay Finny Annabelle 15.1           
repay Elon Finny 65.75               
repay Billibob Carlos 180.33         
repay Annabelle Elon 83.48           
repay Billibob Elon 41.65            
repay Diana Elon 12.37               
borrow Carlos Billibob 183.97        
repay Finny Elon 15.10               
borrow Billibob Finny 19.63          
borrow Billibob Elon 133.19          
repay Annabelle Billibob 181.1       
repay Billibob Elon 31.5             
repay Billibob Finny 160.26          
borrow Annabelle Diana 8.82          
borrow Billibob Finny 116.77         
repay Elon Billibob 61.74            
borrow Billibob Elon 136.0           
borrow Elon Finny 165.92             
repay Annabelle Elon 192.79          
borrow Elon Diana 173.62             
repay Annabelle Billibob 199.60      
borrow Finny Annabelle 46.95"""


newArray = []
array = string.split("\n")
for x in array:
    newArray.append(x.strip())


newList = [x for x in newArray if "Annabelle" in x and "Billibob" in x]
print(newList)



