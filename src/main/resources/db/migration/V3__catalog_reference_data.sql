insert into ingredients (id, code, name, unit) values
    (1, 'coffee_beans', 'Кофейные зёрна', 'G'),
    (2, 'water', 'Вода', 'ML'),
    (3, 'milk_cow', 'Коровье молоко', 'ML'),
    (4, 'milk_oat', 'Овсяное молоко', 'ML'),
    (5, 'milk_almond', 'Миндальное молоко', 'ML'),
    (6, 'milk_lactose_free', 'Безлактозное молоко', 'ML'),
    (7, 'syrup_vanilla', 'Сироп ваниль', 'ML'),
    (8, 'syrup_salted_caramel', 'Сироп солёная карамель', 'ML'),
    (9, 'syrup_hazelnut', 'Сироп лесной орех', 'ML'),
    (10, 'cinnamon', 'Корица', 'G'),
    (11, 'cocoa', 'Какао-порошок', 'G'),
    (12, 'whipped_cream', 'Взбитые сливки', 'G'),
    (13, 'marshmallow', 'Маршмеллоу', 'PCS'),
    (14, 'matcha', 'Матча', 'G');

insert into modifiers (id, code, name, category, price_rubles, ingredient_id, quantity_per_unit) values
    (1, 'milk_cow', 'Коровье молоко', 'MILK', 0, 3, 0),
    (2, 'milk_oat', 'Овсяное молоко', 'MILK', 60, 4, 0),
    (3, 'milk_almond', 'Миндальное молоко', 'MILK', 70, 5, 0),
    (4, 'milk_lactose_free', 'Безлактозное молоко', 'MILK', 40, 6, 0),
    (5, 'syrup_vanilla', 'Сироп ваниль', 'SYRUP', 40, 7, 15),
    (6, 'syrup_salted_caramel', 'Сироп солёная карамель', 'SYRUP', 40, 8, 15),
    (7, 'syrup_hazelnut', 'Сироп лесной орех', 'SYRUP', 40, 9, 15),
    (8, 'extra_shot', 'Дополнительный шот эспрессо', 'EXTRA_SHOT', 50, 1, 9),
    (9, 'topping_cinnamon', 'Корица', 'TOPPING', 15, 10, 1),
    (10, 'topping_cocoa', 'Какао', 'TOPPING', 15, 11, 2),
    (11, 'topping_whipped_cream', 'Взбитые сливки', 'TOPPING', 50, 12, 20),
    (12, 'topping_marshmallow', 'Маршмеллоу', 'TOPPING', 30, 13, 5);

insert into base_drinks (id, code, name, description, milk_portion, default_milk_modifier_id) values
    (1, 'espresso', 'Эспрессо', 'Классический двойной эспрессо', 0, null),
    (2, 'americano', 'Американо', 'Эспрессо, разбавленный горячей водой; молоко по желанию', 30, null),
    (3, 'cappuccino', 'Капучино', 'Эспрессо с молоком и плотной молочной пеной', 150, 1),
    (4, 'latte', 'Латте', 'Эспрессо с большим количеством молока', 200, 1),
    (5, 'matcha_latte', 'Матча латте', 'Японский чай матча с молоком', 200, 1);

insert into base_drink_sizes (base_drink_id, size, price_rubles, volume_ml, recipe_scale_percent) values
    (1, 'S', 150, 40, 100),
    (2, 'S', 170, 250, 100),
    (2, 'M', 200, 350, 140),
    (2, 'L', 230, 450, 180),
    (3, 'S', 220, 250, 100),
    (3, 'M', 260, 350, 140),
    (3, 'L', 300, 450, 180),
    (4, 'S', 230, 300, 100),
    (4, 'M', 270, 400, 130),
    (4, 'L', 310, 500, 160),
    (5, 'M', 320, 350, 100),
    (5, 'L', 360, 450, 130);

insert into base_drink_temperatures (base_drink_id, temperature) values
    (1, 'HOT'),
    (2, 'HOT'), (2, 'ICED'),
    (3, 'HOT'), (3, 'ICED'),
    (4, 'HOT'), (4, 'ICED'),
    (5, 'HOT'), (5, 'ICED');

insert into base_drink_ingredients (base_drink_id, ingredient_id, quantity) values
    (1, 1, 18), (1, 2, 40),
    (2, 1, 9), (2, 2, 200),
    (3, 1, 18),
    (4, 1, 18),
    (5, 14, 4);

insert into base_drink_modifier_limits (base_drink_id, category, min_count, max_count) values
    (1, 'SYRUP', 0, 1), (1, 'EXTRA_SHOT', 0, 1),
    (2, 'MILK', 0, 1), (2, 'SYRUP', 0, 2), (2, 'EXTRA_SHOT', 0, 2),
    (3, 'MILK', 1, 1), (3, 'SYRUP', 0, 3), (3, 'EXTRA_SHOT', 0, 2), (3, 'TOPPING', 0, 2),
    (4, 'MILK', 1, 1), (4, 'SYRUP', 0, 3), (4, 'EXTRA_SHOT', 0, 2), (4, 'TOPPING', 0, 1),
    (5, 'MILK', 1, 1), (5, 'SYRUP', 0, 2), (5, 'TOPPING', 0, 1);

select setval(pg_get_serial_sequence('ingredients', 'id'), (select max(id) from ingredients));
select setval(pg_get_serial_sequence('modifiers', 'id'), (select max(id) from modifiers));
select setval(pg_get_serial_sequence('base_drinks', 'id'), (select max(id) from base_drinks));
