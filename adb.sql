-- phpMyAdmin SQL Dump
-- version 4.2.7.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jan 23, 2015 at 07:44 PM
-- Server version: 5.6.20
-- PHP Version: 5.5.15

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `adb`
--

-- --------------------------------------------------------

--
-- Table structure for table `routes`
--

CREATE TABLE IF NOT EXISTS `routes` (
`route_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `grade` char(1) NOT NULL,
  `terrain` varchar(15) DEFAULT NULL,
  `route` polygon NOT NULL,
  `distance` float NOT NULL,
  `date_created` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `route_results`
--

CREATE TABLE IF NOT EXISTS `route_results` (
`result_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `route_id` int(11) NOT NULL,
  `experience` int(11) NOT NULL,
  `distance` double NOT NULL,
  `max_speed` int(11) NOT NULL,
  `average_speed` int(11) NOT NULL,
  `route_time` time NOT NULL,
  `calories_lost` int(11) NOT NULL,
  `date` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
`user_id` int(11) NOT NULL,
  `email` varchar(40) NOT NULL,
  `hashed_password` char(60) NOT NULL,
  `grade` char(1) DEFAULT NULL,
  `experience` int(11) DEFAULT '50',
  `total_distance_km` float DEFAULT '0',
  `total_time` time DEFAULT '00:00:00',
  `total_calories` int(11) DEFAULT '0',
  `max_speed` float DEFAULT '0',
  `avg_speed` float DEFAULT '0',
  `date_updated` date NOT NULL,
  `date_created` datetime NOT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `email`, `hashed_password`, `grade`, `experience`, `total_distance_km`, `total_time`, `total_calories`, `max_speed`, `avg_speed`, `date_updated`, `date_created`) VALUES
(1, 'abc@123.com', '$2y$10$1XrvyNp6Tg32jw4wmOl/VuwsGjG35N2wCTI4kWXQtAGyEUltvUbLO', NULL, 50, 0, '00:00:00', 0, 0, 0, '0000-00-00', '2015-01-21 13:36:15');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `routes`
--
ALTER TABLE `routes`
 ADD PRIMARY KEY (`route_id`), ADD UNIQUE KEY `CyclistID` (`user_id`);

--
-- Indexes for table `route_results`
--
ALTER TABLE `route_results`
 ADD PRIMARY KEY (`result_id`), ADD UNIQUE KEY `CyclistID` (`user_id`), ADD UNIQUE KEY `ResultID` (`result_id`,`user_id`,`experience`,`distance`,`max_speed`,`average_speed`,`route_time`,`calories_lost`,`date`), ADD UNIQUE KEY `RouteID` (`route_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
 ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `routes`
--
ALTER TABLE `routes`
MODIFY `route_id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `route_results`
--
ALTER TABLE `route_results`
MODIFY `result_id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `routes`
--
ALTER TABLE `routes`
ADD CONSTRAINT `routes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `route_results`
--
ALTER TABLE `route_results`
ADD CONSTRAINT `route_results_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
ADD CONSTRAINT `route_results_ibfk_2` FOREIGN KEY (`route_id`) REFERENCES `routes` (`route_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
