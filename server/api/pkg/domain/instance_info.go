package domain

type InstanceInfo struct {
	Host        string  `json:"host"`
	Name        *string `json:"name"`
	Description *string `json:"description"`
}