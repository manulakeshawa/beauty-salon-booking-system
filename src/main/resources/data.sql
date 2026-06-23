-- Demo seed data for MySQL/JPA storage.
-- SeedDataInitializer runs this file only when the database has no app data
-- and the demo-data seed marker has not been recorded.
-- Each insert is still guarded by a natural key check and INSERT IGNORE.

SET NAMES utf8mb4;

-- Customers moved from customers.txt
INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1001, 'Amaya Fernando', 'amaya.f@email.com', 'amaya123', 'Premium'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('amaya.f@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1002, 'Dineth Perera', 'dineth.p@email.com', 'dineth678', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('dineth.p@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1003, 'Kavindi Silva', 'kavindi.s@email.com', 'kavindi@234', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('kavindi.s@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1004, 'Roshan Jayawardena', 'roshan.j@email.com', 'roshan_3d', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('roshan.j@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1005, 'Nethmi De Silva', 'nethmi.dk@email.com', '$nethmi45', 'Premium'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('nethmi.dk@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1006, 'Sudeera Shasanka', 'hpvictus753@gmail.com', '@Su12red3', 'Premium'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('hpvictus753@gmail.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1007, 'Sarah-Jane De Silva', 'sarah.jane+test@salonweb.com', 'TestAccount1!', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('sarah.jane+test@salonweb.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1008, 'Sarah Jenkins', 'sarah.j@email.com', 'pass123', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('sarah.j@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1009, 'Isabella Silva', 'isabellsil12@email.com', 'bella1234', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('isabellsil12@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1010, 'Soma Kumary', 'somakumary@email.com', 'dutchythin', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('somakumary@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1011, 'Ravindu Perera', 'ravi.the@email.com', '123456', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('ravi.the@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1012, 'testing person', 'testing@email.com', 'testing123', 'Premium'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('testing@email.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1014, 'Emma Thompson', 'emma@test.com', 'c', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('emma@test.com'));

INSERT IGNORE INTO `customers` (`user_id`, `name`, `email`, `password`, `customer_type`)
SELECT 1015, 'Mighara Akash', 'mighara@email.com', 'mighara1234', 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM `customers` WHERE LOWER(`email`) = LOWER('mighara@email.com'));

-- Staff/employee demo data
INSERT IGNORE INTO `employees` (`user_id`, `username`, `password`, `name`, `email`, `role`, `level`, `specialty`, `welcome_message`, `availability_status`)
SELECT 9001, 'admin', 'lumiere2026', 'Salon Owner', 'admin@lumieresalon.lk', 'MANAGER', 'Owner', 'Management', 'Welcome to Lumiere.', 'Available'
WHERE NOT EXISTS (
    SELECT 1 FROM `employees`
    WHERE LOWER(`username`) = LOWER('admin') OR LOWER(`email`) = LOWER('admin@lumieresalon.lk')
);

-- Stylists moved from stylists.txt
INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4001, 'Nalika', 'nalika@lumieresalon.lk', '#nalika@lume', 'Skin Rejuvenation', 'Lead', TRUE, 'Skin Rejuvenation.png'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('nalika@lumieresalon.lk'));

INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4002, 'Kasun', 'kasun@lumieresalon.lk', '#kasun@lume', 'Color & Balayage', 'Senior', TRUE, 'Color & Balayage.png'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('kasun@lumieresalon.lk'));

INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4003, 'Kamindu', 'kamindu@lumieresalon.lk', '#kamindu@lume', 'Men''s Grooming', 'Master', TRUE, 'Men''s Grooming.png'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('kamindu@lumieresalon.lk'));

INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4004, 'Shenali', 'shenali@lumieresalon.lk', '#shenali@lume', 'Hair Treatments', 'Senior', TRUE, 'Hair Treatments.png'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('shenali@lumieresalon.lk'));

INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4005, 'Dinithi', 'dinithi@lumieresalon.lk', '#dinithi@lume', 'Nail Artistry', 'Junior', TRUE, 'Nail Artistry.png'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('dinithi@lumieresalon.lk'));

INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4006, 'Ruwan', 'ruwan@lumieresalon.lk', '#ruwan@lume', 'Massage Therapy', 'Senior', TRUE, 'Massage Therapy.png'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('ruwan@lumieresalon.lk'));

INSERT IGNORE INTO `stylists` (`user_id`, `name`, `email`, `password`, `specialty`, `level`, `available`, `image_file_name`)
SELECT 4007, 'Zara', 'zara@lumieresalon.lk', '#zara@lume', 'Hair Extensions & Volume', 'Lead', TRUE, 'default.jpg'
WHERE NOT EXISTS (SELECT 1 FROM `stylists` WHERE LOWER(`email`) = LOWER('zara@lumieresalon.lk'));

-- Services moved from data/services.txt
INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2001, 'Standard', 'Lumière Signature Facial', 'Deep cleansing and rejuvenating spa facial for radiant skin.', 8500.0, 'facial.png', 'Nalika'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Lumière Signature Facial'));

INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2002, 'Standard', 'Bespoke Hair Styling', 'Premium haircut and styling consultation by a master stylist.', 5000.0, 'haircut.png', 'Shenali'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Bespoke Hair Styling'));

INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2003, 'Standard', 'Luxury Gel Manicure', 'Complete hand care with cuticle treatment and premium gel polish.', 4500.0, 'manicure.png', 'Dinithi'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Luxury Gel Manicure'));

INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2004, 'Package', 'Keratin Smoothing Treatment', 'Long-lasting frizz control and luxury hair smoothing.', 15000.0, 'keratin.png', 'Shenali'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Keratin Smoothing Treatment'));

INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2005, 'Package', 'Bridal Hair & Makeup Package', 'Full bridal preparation including initial consultation.', 35000.0, 'bridal.png', 'Kasun'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Bridal Hair & Makeup Package'));

INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2006, 'Standard', 'Classic Gentlemen''s Grooming', 'Traditional hot towel shave and precision haircut.', 3500.0, 'grooming.png', 'Kamindu'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Classic Gentlemen''s Grooming'));

INSERT IGNORE INTO `salon_services` (`service_id`, `service_type`, `name`, `description`, `base_price`, `image_file_name`, `stylist_name`)
SELECT 2007, 'Package', 'Signature Volumizing Hair Extensions', 'A complete transformation adding luxurious length and natural-looking volume using premium ethically sourced extensions. Includes custom color matching seamless blending and a signature styling finish.', 45000.0, 'hairextention.png', 'Zara'
WHERE NOT EXISTS (SELECT 1 FROM `salon_services` WHERE LOWER(`name`) = LOWER('Signature Volumizing Hair Extensions'));

-- Appointments moved from data/appointments.txt
INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3001, 'Amaya Fernando', 'Lumière Signature Facial', 'Nalika', '2026-04-20', '10:00', 'Confirmed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Amaya Fernando') AND LOWER(`service_name`) = LOWER('Lumière Signature Facial') AND LOWER(`stylist_name`) = LOWER('Nalika') AND `appointment_date` = '2026-04-20' AND `appointment_time` = '10:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3002, 'Dineth Perera', 'Classic Gentlemen''s Grooming', 'Kamindu', '2026-04-20', '11:30', 'Confirmed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Dineth Perera') AND LOWER(`service_name`) = LOWER('Classic Gentlemen''s Grooming') AND LOWER(`stylist_name`) = LOWER('Kamindu') AND `appointment_date` = '2026-04-20' AND `appointment_time` = '11:30');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3003, 'Kavindi Silva', 'Bridal Hair & Makeup Package', 'Kasun', '2026-04-21', '08:30', 'Checked In'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Kavindi Silva') AND LOWER(`service_name`) = LOWER('Bridal Hair & Makeup Package') AND LOWER(`stylist_name`) = LOWER('Kasun') AND `appointment_date` = '2026-04-21' AND `appointment_time` = '08:30');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3004, 'Roshan Jayawardena', 'Bespoke Hair Styling', 'Kasun', '2026-04-21', '14:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Roshan Jayawardena') AND LOWER(`service_name`) = LOWER('Bespoke Hair Styling') AND LOWER(`stylist_name`) = LOWER('Kasun') AND `appointment_date` = '2026-04-21' AND `appointment_time` = '14:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3005, 'Nethmi De Silva', 'Luxury Gel Manicure', 'Dinithi', '2026-04-22', '10:30', 'Cancelled'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Nethmi De Silva') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `appointment_date` = '2026-04-22' AND `appointment_time` = '10:30');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3006, 'Sudeera Shasanka', 'Keratin Smoothing Treatment', 'Shenali', '2026-04-22', '16:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sudeera Shasanka') AND LOWER(`service_name`) = LOWER('Keratin Smoothing Treatment') AND LOWER(`stylist_name`) = LOWER('Shenali') AND `appointment_date` = '2026-04-22' AND `appointment_time` = '16:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3007, 'Sarah-Jane De Silva', 'Bridal Hair & Makeup Package', 'Shenali', '2026-04-30', '18:00', 'Confirmed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sarah-Jane De Silva') AND LOWER(`service_name`) = LOWER('Bridal Hair & Makeup Package') AND LOWER(`stylist_name`) = LOWER('Shenali') AND `appointment_date` = '2026-04-30' AND `appointment_time` = '18:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3008, 'Sarah Jenkins', 'Keratin Smoothing Treatment', 'Shenali', '2026-04-30', '10:30', 'Cancelled'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sarah Jenkins') AND LOWER(`service_name`) = LOWER('Keratin Smoothing Treatment') AND LOWER(`stylist_name`) = LOWER('Shenali') AND `appointment_date` = '2026-04-30' AND `appointment_time` = '10:30');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3009, 'Sarah Jenkins', 'Bespoke Hair Styling', 'Kasun', '2026-04-27', '11:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sarah Jenkins') AND LOWER(`service_name`) = LOWER('Bespoke Hair Styling') AND LOWER(`stylist_name`) = LOWER('Kasun') AND `appointment_date` = '2026-04-27' AND `appointment_time` = '11:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3010, 'Isabella Silva', 'Lumière Signature Facial', 'Nalika', '2026-05-05', '13:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Isabella Silva') AND LOWER(`service_name`) = LOWER('Lumière Signature Facial') AND LOWER(`stylist_name`) = LOWER('Nalika') AND `appointment_date` = '2026-05-05' AND `appointment_time` = '13:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3011, 'Sarah Jenkins', 'Luxury Gel Manicure', 'Dinithi', '2026-04-07', '12:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sarah Jenkins') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `appointment_date` = '2026-04-07' AND `appointment_time` = '12:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3012, 'Soma Kumary', 'Lumière Signature Facial', 'Nalika', '2026-04-25', '14:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Soma Kumary') AND LOWER(`service_name`) = LOWER('Lumière Signature Facial') AND LOWER(`stylist_name`) = LOWER('Nalika') AND `appointment_date` = '2026-04-25' AND `appointment_time` = '14:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3013, 'Soma Kumary', 'Keratin Smoothing Treatment', 'Shenali', '2026-04-28', '09:00', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Soma Kumary') AND LOWER(`service_name`) = LOWER('Keratin Smoothing Treatment') AND LOWER(`stylist_name`) = LOWER('Shenali') AND `appointment_date` = '2026-04-28' AND `appointment_time` = '09:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3014, 'Sudeera Shasanka', 'Classic Gentlemen''s Grooming', 'Kamindu', '2026-04-29', '10:55', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sudeera Shasanka') AND LOWER(`service_name`) = LOWER('Classic Gentlemen''s Grooming') AND LOWER(`stylist_name`) = LOWER('Kamindu') AND `appointment_date` = '2026-04-29' AND `appointment_time` = '10:55');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3015, 'Sudeera Shasanka', 'Luxury Gel Manicure', 'Dinithi', '2026-05-01', '12:00', 'Confirmed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sudeera Shasanka') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `appointment_date` = '2026-05-01' AND `appointment_time` = '12:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3016, 'Ravindu Perera', 'Classic Gentlemen''s Grooming', 'Kamindu', '2026-05-01', '10:27', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Ravindu Perera') AND LOWER(`service_name`) = LOWER('Classic Gentlemen''s Grooming') AND LOWER(`stylist_name`) = LOWER('Kamindu') AND `appointment_date` = '2026-05-01' AND `appointment_time` = '10:27');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3017, 'Ravindu Perera', 'Luxury Gel Manicure', 'Dinithi', '2026-05-01', '10:53', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Ravindu Perera') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `appointment_date` = '2026-05-01' AND `appointment_time` = '10:53');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3018, 'Ravindu Perera', 'Bridal Hair & Makeup Package', 'Kasun', '2026-05-08', '13:00', 'Cancelled'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Ravindu Perera') AND LOWER(`service_name`) = LOWER('Bridal Hair & Makeup Package') AND LOWER(`stylist_name`) = LOWER('Kasun') AND `appointment_date` = '2026-05-08' AND `appointment_time` = '13:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3019, 'Mighara Akash', 'Classic Gentlemen''s Grooming', 'Kamindu', '2026-05-10', '12:07', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Mighara Akash') AND LOWER(`service_name`) = LOWER('Classic Gentlemen''s Grooming') AND LOWER(`stylist_name`) = LOWER('Kamindu') AND `appointment_date` = '2026-05-10' AND `appointment_time` = '12:07');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3020, 'Mighara Akash', 'Signature Volumizing Hair Extensions', 'Zara', '2026-05-10', '12:15', 'Cancelled'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Mighara Akash') AND LOWER(`service_name`) = LOWER('Signature Volumizing Hair Extensions') AND LOWER(`stylist_name`) = LOWER('Zara') AND `appointment_date` = '2026-05-10' AND `appointment_time` = '12:15' AND LOWER(`status`) = LOWER('Cancelled'));

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3021, 'Mighara Akash', 'Signature Volumizing Hair Extensions', 'Zara', '2026-05-10', '12:15', 'Completed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Mighara Akash') AND LOWER(`service_name`) = LOWER('Signature Volumizing Hair Extensions') AND LOWER(`stylist_name`) = LOWER('Zara') AND `appointment_date` = '2026-05-10' AND `appointment_time` = '12:15' AND LOWER(`status`) = LOWER('Completed'));

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3022, 'Sarah Jenkins', 'Lumière Signature Facial', 'Nalika', '2026-05-26', '10:00', 'Confirmed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Sarah Jenkins') AND LOWER(`service_name`) = LOWER('Lumière Signature Facial') AND LOWER(`stylist_name`) = LOWER('Nalika') AND `appointment_date` = '2026-05-26' AND `appointment_time` = '10:00');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3023, 'saman', 'Lumière Signature Facial', 'Nalika', '2026-05-26', '10:59', 'Confirmed'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('saman') AND LOWER(`service_name`) = LOWER('Lumière Signature Facial') AND LOWER(`stylist_name`) = LOWER('Nalika') AND `appointment_date` = '2026-05-26' AND `appointment_time` = '10:59');

INSERT IGNORE INTO `appointments` (`appointment_id`, `customer_name`, `service_name`, `stylist_name`, `appointment_date`, `appointment_time`, `status`)
SELECT 3024, 'Mighara Akash', 'Luxury Gel Manicure', 'Dinithi', '2026-05-11', '10:00', 'Pending'
WHERE NOT EXISTS (SELECT 1 FROM `appointments` WHERE LOWER(`customer_name`) = LOWER('Mighara Akash') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `appointment_date` = '2026-05-11' AND `appointment_time` = '10:00');

-- Reviews moved from reviews.txt
INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5001, 'Amaya Fernando', 'Lumière Signature Facial', 'Nalika', 4, 'Absolutely incredible experience. My skin has never felt better!', 'Public', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Amaya Fernando') AND LOWER(`service_name`) = LOWER('Lumière Signature Facial') AND LOWER(`stylist_name`) = LOWER('Nalika') AND `rating` = 4 AND `comment` = 'Absolutely incredible experience. My skin has never felt better!');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5002, 'Dineth Perera', 'Classic Gentlemen''s Grooming', 'Kamindu', 5, 'Kamindu is a true professional. The hot towel shave was top notch.', 'Public', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Dineth Perera') AND LOWER(`service_name`) = LOWER('Classic Gentlemen''s Grooming') AND LOWER(`stylist_name`) = LOWER('Kamindu') AND `rating` = 5 AND `comment` = 'Kamindu is a true professional. The hot towel shave was top notch.');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5003, 'Kavindi Silva', 'Bridal Hair & Makeup Package', 'Kasun', 5, 'Kasun made me look flawless for my big day. Highly recommend this salon!', 'Public', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Kavindi Silva') AND LOWER(`service_name`) = LOWER('Bridal Hair & Makeup Package') AND LOWER(`stylist_name`) = LOWER('Kasun') AND `rating` = 5 AND `comment` = 'Kasun made me look flawless for my big day. Highly recommend this salon!');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5004, 'Roshan Jayawardena', 'Bespoke Hair Styling', 'Kasun', 4, 'Great sharp cut. The salon has a wonderful and relaxing atmosphere.', 'Public', FALSE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Roshan Jayawardena') AND LOWER(`service_name`) = LOWER('Bespoke Hair Styling') AND LOWER(`stylist_name`) = LOWER('Kasun') AND `rating` = 4 AND `comment` = 'Great sharp cut. The salon has a wonderful and relaxing atmosphere.');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5005, 'Nethmi De Silva', 'Luxury Gel Manicure', 'Dinithi', 5, 'Dinithi is a perfectionist. The gel polish looks flawless and vibrant.', 'Public', FALSE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Nethmi De Silva') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `rating` = 5 AND `comment` = 'Dinithi is a perfectionist. The gel polish looks flawless and vibrant.');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5006, 'Sudeera Shasanka', 'Keratin Smoothing Treatment', 'Shenali', 5, 'Very professional service. My hair is incredibly smooth, shiny, and easy to manage now.', 'Verified', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Sudeera Shasanka') AND LOWER(`service_name`) = LOWER('Keratin Smoothing Treatment') AND LOWER(`stylist_name`) = LOWER('Shenali') AND `rating` = 5 AND `comment` = 'Very professional service. My hair is incredibly smooth, shiny, and easy to manage now.');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5007, 'Sarah-Jane De Silva', 'Bridal Hair & Makeup Package', 'Shenali', 5, 'Stunning results and professional service. I couldn''t have asked for a better bridal glow-up!', 'Verified', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Sarah-Jane De Silva') AND LOWER(`service_name`) = LOWER('Bridal Hair & Makeup Package') AND LOWER(`stylist_name`) = LOWER('Shenali') AND `rating` = 5 AND `comment` = 'Stunning results and professional service. I couldn''t have asked for a better bridal glow-up!');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5008, 'Ravindu Perera', 'Luxury Gel Manicure', 'Dinithi', 5, 'The Best Luxury Gel Manicure I''ve Ever Had!', '8a7c8a82-9c18-48ca-a631-d846dfe741e2', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Ravindu Perera') AND LOWER(`service_name`) = LOWER('Luxury Gel Manicure') AND LOWER(`stylist_name`) = LOWER('Dinithi') AND `rating` = 5 AND `comment` = 'The Best Luxury Gel Manicure I''ve Ever Had!');

INSERT IGNORE INTO `reviews` (`review_id`, `customer_name`, `service_name`, `stylist_name`, `rating`, `comment`, `owner_token`, `verified`)
SELECT 5009, 'Mighara Akash', 'Classic Gentlemen''s Grooming', 'Kamindu', 4, 'I recommend to anyone. I''m so happy..', 'f0c9a6f0-1862-4c0a-98a4-50e5f08d543c', TRUE
WHERE NOT EXISTS (SELECT 1 FROM `reviews` WHERE LOWER(`customer_name`) = LOWER('Mighara Akash') AND LOWER(`service_name`) = LOWER('Classic Gentlemen''s Grooming') AND LOWER(`stylist_name`) = LOWER('Kamindu') AND `rating` = 4 AND `comment` = 'I recommend to anyone. I''m so happy..');
