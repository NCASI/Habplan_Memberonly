#' Convert Carbon data frames to Habplan-suitable flow files
#'
#' Creates flow files from csv input Carbon data
#' @param std.data Forest stand, regime, and outcome information
#' @param std.info Additional stand information
#' @param col The column number for the data interested in from std.data
#' @param nyear Number of years or time periods interested in (have data for)
#' @param HSI Threshold value of HSI for species of interest (0-0.99)
#' @return Saves a .dat file in the working directory ready for import to Habplan
#' @examples
#' #Read in stand and regime information
#' std.data <- read_csv("./fvs_results.csv")
#'
#' #Read in additional stand information - acreage, id, and description
#' std.info <- read_csv("./Stand_info.csv")
#'
#' #Look at the column headings to decide which column has flow results
#' colnames(new.data)
#'
#' #From the column names, we are interested in "HSI", which is
#' #column number 37. We can input this into our function.
#'
#' #Run function
#' rcw.flow <- habConvert(std.data = new.data, std.info = std.info, col = 37,
#'                        nyear = 35, HSI = 0.7)
#'
#' @export

CarbonConvert <- function(std.data){

  #We need to extract year, stand id and flow

  #Get each stand name to run through loop
  std.id <- unique(std.data$StandKey)

  #Create list to store stand data
  std.list <- vector(mode = "list", length = length(std.id))

  std.data <- std.data[order(std.data$Age, decreasing = F),]
  std.data <- std.data[order(std.data$RGM, decreasing = F),]
  std.data <- std.data[order(std.data$StandKey, decreasing = F),]

  #age.list <- list()
  #for (a in 1:length(std.list)) {
  #  age.data <- std.data %>%
  #    filter(StandKey == std.id[a])
  #  ages <- unique(age.data$Age)
  #  age.list[[a]] <- ages
  #}
  #test <- data.frame(unlist(age.list))
  #yr.list <- unique(test)
  #test2 <- test %>%
  #  group_by(unlist.age.list.) %>%
  #  tally()

  #cnt.list <- list()
  #for (c in 1:length(yr.list)) {
  #  cnt.test <- str_count(test, paste0("'", c, "'"))
  #  cnt.list[[]] <- cnt.test
  #}

  #library(stringr)

  for (j in 1:length(std.list)) {
    #j <- 7
    temp.data <- std.data %>%
      filter(StandKey == std.id[j])

    regim <- unique(temp.data$RGM)
    #col <- 14
    #flow.var <- temp.data[col]

    reg.list <- vector(mode = "list", length = length(regim))

    for (i in 1:length(regim)) {
      #i <- 1

      years.1 <- unique(std.data$Yr)
      years.1 <- sort(years.1)

      #years

      carb.data <- data.frame(temp.data[,5], temp.data[,4], temp.data[,2], temp.data[,28])

      #carb.data <- data.frame(cbind(temp.data$Yr, temp.data$RGM, temp.data$StandKey, temp.data$C_AG))
      colnames(carb.data) <- c("year", "regime", "std_id", "flow")
      #class(carb.data$year)
      #Need to replace all of the NAs to 0s
      #library(dplyr)
      #carb.data <- carb.data %>%
        #replace(is.na(.), 0)
      carb.data <- carb.data[order(carb.data$year, decreasing = F),]


      carb.data.2 <- carb.data %>%
        filter(regime == regim[i])

      yr.diff <- setdiff(years.1, carb.data.2$year)

      #summary(carb.data.2)

      #Now need to take difference in years and add them as rows to new dataframe
     # if(length(yr.diff) > 0){
       # for (k in 1:length(yr.diff)) {
          #i = 1
          #j = 1
          new.rows <- data.frame(yr.diff)
          if(length(yr.diff) > 0) {
          new.rows[,2] <- regim[i]
          new.rows[,3] <- std.id[j]
          new.rows[,4] <- min(carb.data.2$flow)
          colnames(new.rows) <- c("year", "regime", "std_id", "flow")

          #new.row <- list(year = as.numeric(yr.diff[k]), regime = as.character(regim[i]),
                          #std_id = as.character(std.id[j]), flow = 0)
          #carb.data.2[nrow(carb.data.2) + 1, names(new.row)] <- new.row
      #  }
     # } else {

     # }

      carb.data.2 <- rbind(carb.data.2, new.rows)
          } else {

          }

      #class(carb.data.2[35,4])

      carb.data.2 <- carb.data.2[order(carb.data.2$year, decreasing = F),]
      #carb.data.2$year <- 1:nrow(carb.data.2)

      #Go through the filled data frame to change 0 flow values
      #class(carb.data.2$year)

      #new.test <- carb.data.2 %>%
      #  filter(if_any(.cols = flow, ~ .x > 0))

      #for(u in 1:nrow(carb.data.2)){
      #  if(carb.data.2[u,4] == 0){
      #    carb.data.2[u,4] <- as.numeric (min(new.test$flow)) + (as.numeric(u)/100)
      #  }else {
#
 #       }
    #  }

      #summary(carb.data.2)
      carb.data.2$year <- as.numeric(carb.data.2$year)
      rownames(carb.data.2) <- 1:nrow(carb.data.2)

      years.2 <- 1:nrow(carb.data.2)
      years.2 <- as.character(years.2)
      years.2 <- toString(years.2)
      #Remove commas from string of years since habplan cant read them
      years.2 <- gsub('[,]', '', years.2)
      #class(years.2)



      #Now get flow results for those years
      flows <- c(carb.data.2[(1:length(carb.data.2$year)), (4:4)])
      #class(flows)

      #Get the acreage for the std_id
      #std.name <- unique(carb.data.2$std_id)
      #std.in <- std.info %>%
      #  filter(std_id == std.name)
      #acres <- std.in[[1, 2]]
      #if(colnames(temp.data[col]) == "HSI"){
      #  flows <- flows
      #} else{
      #  flows <- flows*acres
      #}

      flows <- as.character(flows)
      flows <- toString(flows)
      #Remove commas from string of flows since habplan cant read them
      flows <- gsub('[,]', '', flows)


      id <- unique(carb.data.2$std_id)
      reg <- unique(carb.data.2$regime)

      data <- paste0(id, ' ', reg, ' ', years.2, ' ', flows)
      #class(data)
      reg.list[[i]] <- data
    }
    vec <- unlist(reg.list)
    #vec
    std.list[[j]] <- vec
  }
  final.data <- unlist(std.list)
  #summary(final.data)
  data.file <- file(paste0("./Carbon_test.dat"))
  writeLines(final.data, data.file)
  close(data.file)
  return(final.data)
}
