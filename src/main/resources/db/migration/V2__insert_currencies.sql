insert into supported_currencies(currency) values ('USD');
insert into supported_currencies(currency) values ('EUR');
insert into supported_currencies(currency) values ('GBP');

insert into currency_exchange_rate(source_currency, target_currency, exchange_rate) values('EUR', 'USD', 1.1100);
insert into currency_exchange_rate(source_currency, target_currency, exchange_rate) values('EUR', 'GBP', 0.8500);
insert into currency_exchange_rate(source_currency, target_currency, exchange_rate) values('USD', 'GBP', 0.7700);
insert into currency_exchange_rate(source_currency, target_currency, exchange_rate) values('USD', 'EUR', 0.9000);
insert into currency_exchange_rate(source_currency, target_currency, exchange_rate) values('GBP', 'EUR', 1.1700);
insert into currency_exchange_rate(source_currency, target_currency, exchange_rate) values('GBP', 'USD', 1.3000);