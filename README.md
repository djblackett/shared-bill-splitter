# shared-bill-splitter


List of commands
```
> help
balance
borrow
cashBack
exit
group
help
purchase
repay
secretSanta
writeOff
```


Work with Spring boot console application.
Process the following command:
[date] balance [open|close] [(list of [+|-] persons | GROUPS)]
Process complex shared purchases and repayments.
Calculate and display the list of repayments with names and amounts to be repaid.


Usage is most easily seen by example. Note that lines beginning with `>` are user input while all others are program output. 

```
> writeOff
> group create TEAM (Ann, Bob, Chuck, Diana, Elon, Frank)
> group create CAR (Diana, Elon)
> group create BUS (Ann, Bob, Chuck, Frank)
> purchase Chuck busTickets 5.25 (BUS, -Frank)
> purchase Elon fuel 25 (CAR, Frank)
> purchase Ann chocolate 2.99 (BUS, -Bob, CAR)
> purchase Diana soda 5.45 (TEAM, -Ann, -Chuck)
> purchase Frank bbq 29.90 (TEAM, CAR, BUS, -Frank, -Bob)
> cashBack YourCompany party 12 (TEAM, BUS)
> cashBack YourCompany tickets 3.50 (BUS)
> borrow Frank Bob 10
> repay Chuck Diana 20
> balance close
Ann owes Chuck 1.15
Ann owes Frank 6.89
Bob owes Chuck 1.75
Bob owes Diana 1.37
Chuck owes Frank 7.48
Diana owes Ann 0.60
Diana owes Chuck 20.00
Diana owes Elon 6.98
Diana owes Frank 6.11
Elon owes Ann 0.60
Frank owes Bob 10.00
Frank owes Elon 0.86
YourCompany owes Ann 2.88
YourCompany owes Bob 2.88
YourCompany owes Chuck 2.87
YourCompany owes Diana 2.00
YourCompany owes Elon 2.00
YourCompany owes Frank 2.87
> balance close (-Frank, CAR, Chuck)
Chuck owes Frank 7.48
Diana owes Ann 0.60
Diana owes Chuck 20.00
Diana owes Elon 6.98
Diana owes Frank 6.11
Elon owes Ann 0.60
```
