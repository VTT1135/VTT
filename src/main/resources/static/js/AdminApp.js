var app = angular.module('AdminApp', ['ngRoute']);

// Cấu hình routing
app.config(function($routeProvider) {
    $routeProvider
        .when('/AdminStatistics', {
            templateUrl: '/Admin-views/Admin-Statistics.html',
            controller: 'AdminStatisticsController'
        })
        .when('/AdminUser', {
            templateUrl: '/Admin-views/Admin-Users.html',
            controller: 'AdminUsersController'
        })
        .when('/AdminProducts', {
            templateUrl: '/Admin-views/Admin-Products.html',
            controller: 'AdminProductsController'
        })
        .when('/AdminCategories', {
            templateUrl: '/Admin-views/Admin-Categories.html',
            controller: 'AdminCategoriesController'
        })
        .when('/AdminBrands', {
            templateUrl: '/Admin-views/Admin-Brands.html',
            controller: 'AdminBrandsController'
        })
        .when('/AdminPromotions', {
            templateUrl: '/Admin-views/Admin-Promotions.html',
            controller: 'AdminPromotionsController'
        })
        .when('/AdminOrders', {
            templateUrl: '/Admin-views/Admin-Orders.html',
            controller: 'AdminOrdersController'
        })
        .otherwise({
            redirectTo: '/AdminHome'
        });
});

// Directive to handle file input in AngularJS
app.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

app.controller('AdminProductsController', function($scope, $http) {
    $scope.products = [];
    $scope.categories = [];
    $scope.brands = [];
    $scope.product = {};
    $scope.product.images = [];
    $scope.deletedImages = [];
    $scope.isEdit = false;
    $scope.searchName = '';
    $scope.searchCategoryId = '';
    $scope.searchBrandId = '';
    $scope.currentPage = 1;
    $scope.pageSize = 12;

    // Fetch all categories
    $http.get('/admin/api/categories').then(function(response) {
        $scope.categories = response.data;
    });

    $http.get('/admin/api/brands').then(function(response) {
        $scope.brands = response.data;
    });

    $scope.getImageUrl = function(imageUrl) {
        if (!imageUrl) return '';
        return imageUrl + '?t=' + new Date().getTime();
    };
    // Fetch all products
    $scope.getAllProducts = function() {
        $http.get('/admin/api/products').then(function(response) {
            $scope.products = response.data.map(product => {
                if (product.images && product.images.length > 0) {
                    product.images = product.images.map(function(image) {
                        return {
                            imageUrl: $scope.getImageUrl(image.imageUrl || image)
                        };
                    });
                } else {
                    product.images = [];
                }
                product.imageUrl = product.images[0]?.imageUrl || ''; // Dùng ảnh đầu tiên để hiển thị trong danh sách
                return product;
            });
        });
    };


    $scope.setImages = function(files) {
        $scope.product.images = Array.from(files);
    };

    // Add product
    $scope.addProduct = function() {
        var fd = new FormData();
        fd.append('name', $scope.product.name);
        fd.append('description', $scope.product.description);
        fd.append('price', $scope.product.price);
        fd.append('categoryId', $scope.product.categoryId);
        fd.append('brandId', $scope.product.brandId);

        // Lấy lại tệp ảnh từ input và thêm vào FormData
        var imageFiles = document.getElementById('productImages').files;
        if (imageFiles.length > 0) {
            Array.from(imageFiles).forEach(file => {
                fd.append('images', file); // Thêm tệp vào FormData
            });
        } else {
            showToast('Vui lòng chọn ít nhất một hình ảnh', 'error', 'Lỗi');
            return;
        }

        $http.post('/admin/api/products', fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).then(function(response) {
            var newProduct = response.data;

            // Cập nhật danh sách ảnh từ server
            if (newProduct.images && newProduct.images.length > 0) {
                newProduct.images = newProduct.images.map(function(image) {
                    return {
                        imageUrl: $scope.getImageUrl(image.imageUrl || image) // Đảm bảo lấy toàn bộ danh sách URL
                    };
                });
            } else {
                newProduct.images = [];
            }

            // Gán lại `imageUrl` (chỉ để hiển thị trên danh sách)
            newProduct.imageUrl = newProduct.images[0]?.imageUrl || '';

            // Thêm sản phẩm vào danh sách sản phẩm
            $scope.products.push(newProduct);

            $scope.resetForm();
            showToast(
                'Sản phẩm mới đã được thêm thành công',
                'success',
                'Thêm sản phẩm thành công!'
            );
        }, function(error) {
            showToast(
                'Không thể thêm sản phẩm mới',
                'error',
                'Lỗi hệ thống!'
            );
        });
    };


    // Update product
    $scope.updateProduct = function() {
        var validCategory = $scope.categories.some(c => c.id === $scope.product.categoryId);
        var validBrand = $scope.brands.some(b => b.id === $scope.product.brandId);

        if (!validCategory) {
            showToast('Loại sản phẩm không hợp lệ', 'error', 'Lỗi');
            return;
        }

        if (!validBrand) {
            showToast('Thương hiệu không hợp lệ', 'error', 'Lỗi');
            return;
        }
        var fd = new FormData();
        fd.append('name', $scope.product.name);
        fd.append('description', $scope.product.description);
        fd.append('price', $scope.product.price);
        fd.append('categoryId', $scope.product.categoryId || null);
        fd.append('brandId', $scope.product.brandId || null);

        // Lấy lại tệp ảnh từ input và thêm vào FormData
        var imageFiles = document.getElementById('productImages').files;
        if (imageFiles.length > 0) {
            Array.from(imageFiles).forEach(file => {
                fd.append('images', file); // Thêm tệp vào FormData
            });
        } else {
            showToast('Vui lòng chọn ít nhất một hình ảnh', 'error', 'Lỗi');
            return;
        }
        // Update to append images correctly
        if ($scope.product.images && $scope.product.images.length > 0) {
            for (var i = 0; i < $scope.product.images.length; i++) {
                fd.append('images', $scope.product.images[i]); // Changed from 'image' to 'images'
            }
        }

        if ($scope.deletedImages.length > 0) {
            for (var j = 0; j < $scope.deletedImages.length; j++) {
                fd.append('deletedImages', $scope.deletedImages[j].imageUrl); // Giả sử bạn gửi URL của ảnh đã xóa
            }
        }

        $http.put('/admin/api/products/' + $scope.product.id, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).then(function(response) {
            var updatedProduct = response.data;
            if (updatedProduct.imageUrl) {
                updatedProduct.imageUrl = $scope.getImageUrl(updatedProduct.imageUrl);
            }
            var index = $scope.products.findIndex(p => p.id === updatedProduct.id);
            if (index !== -1) {
                $scope.products[index] = updatedProduct;
            }
            $scope.deletedImages = [];
            $scope.resetForm();
            showToast(
                'Sản phẩm đã được cập nhật thành công',
                'success',
                'Cập nhật thành công!'
            );
        }, function(error) {
            showToast(
                'Không thể cập nhật sản phẩm',
                'error',
                'Lỗi hệ thống!'
            );
        });
    };

    $scope.previewImages = function() {
        var imageFiles = document.getElementById('productImages').files;
        $scope.product.images = []; // Reset danh sách hình ảnh cũ
        Array.from(imageFiles).forEach(file => {
            var reader = new FileReader();
            reader.onload = function(e) {
                if (!$scope.$$phase) {
                    $scope.$apply(function() {
                        $scope.product.images.push(e.target.result);
                    });
                } else {
                    $scope.product.images.push(e.target.result);
                }
            };
            reader.readAsDataURL(file);
        });
    };

    $scope.removeImage = function(image) {
        const index = $scope.product.images.indexOf(image);
        if (index > -1) {
            $scope.deletedImages.push(image); // Lưu ảnh đã xóa vào mảng deletedImages
            $scope.product.images.splice(index, 1); // Xóa ảnh khỏi mảng images
        }
    };

    // Edit product
    $scope.editProduct = function(product) {
        $scope.product = angular.copy(product);
        $scope.product.categoryId = product.category ? product.category.id : null;
        $scope.product.brandId = product.brand ? product.brand.id : null;
        $scope.isEdit = true;

        // Kiểm tra và gán lại giá trị cho product.images
        $scope.product.images = product.images ? product.images.slice() : [];

        // Reset input file
        const fileInput = document.getElementById('productImages');
        if (fileInput) {
            fileInput.value = ''; // Reset giá trị của input
        }

        const editForm = document.querySelector("form");
        if (editForm) {
            editForm.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    };



    // Delete product
    $scope.deleteProduct = function(id) {
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
            $http.delete('/admin/api/products/' + id).then(function(response) {
                $scope.getAllProducts();
                showToast(
                    'Sản phẩm đã được xóa thành công',
                    'success',
                    'Xóa sản phẩm thành công!'
                );
            }, function(error) {
                showToast(
                    'Không thể xóa sản phẩm',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
        }
    };

    $scope.showImages = function(product) {
        $scope.selectedProduct = product; // Set the selected product

        $('#imageViewerModal').modal('show'); // Show the modal
    };

    // Reset form
    $scope.resetForm = function() {
        $scope.product = {};
        $scope.isEdit = false;

        const fileInput = document.getElementById('productImages');
        if (fileInput) {
            fileInput.value = '';
        }
    };

    // Custom function to filter products
    $scope.filteredProducts = function() {
        return $scope.products.filter(function(product) {
            return (!$scope.searchName || product.name.toLowerCase().includes($scope.searchName.toLowerCase())) &&
                (!$scope.searchCategoryId || product.category.id === $scope.searchCategoryId)&&
                (!$scope.searchBrandId || product.brand.id === $scope.searchBrandId);

        });
    };


    // Pagination: Get paginated products
    $scope.paginatedProducts = function() {
        var begin = ($scope.currentPage - 1) * $scope.pageSize;
        return $scope.filteredProducts().slice(begin, begin + $scope.pageSize);
    };

    // Get total number of pages
    $scope.totalPages = function() {
        return Array.from({ length: Math.ceil($scope.filteredProducts().length / $scope.pageSize) }, (_, i) => i + 1);
    };

    // Change page
    $scope.changePage = function(page) {
        if (page >= 1 && page <= $scope.totalPages().length) {
            $scope.currentPage = page;
        }
    };

    // Fetch products on load
    $scope.getAllProducts();
});

app.controller('AdminCategoriesController', function($scope, $http) {
    $scope.categories = [];
    $scope.category = {};
    $scope.isEdit = false;

    // Fetch all categories
    $scope.getAllCategories = function() {
        $http.get('/admin/api/categories').then(function(response) {
            $scope.categories = response.data;
        }, function(error) {
            console.error('Error fetching categories', error);
        });
    };

    // Save new category
    $scope.saveCategory = function() {
        if (!$scope.isEdit) {
            // Create new category
            $http.post('/admin/api/categories', $scope.category)
                .then(function(response) {
                    $scope.getAllCategories();
                    $scope.resetForm();
                    showToast(
                        'Danh mục mới đã được thêm thành công',
                        'success',
                        'Thêm danh mục thành công!'
                    );
                }, function(error) {
                    showToast(
                        'Không thể thêm danh mục mới',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Update category
    $scope.updateCategory = function() {
        // Update category
        $http.put('/admin/api/categories/' + $scope.category.id, $scope.category)
            .then(function(response) {
                $scope.getAllCategories();
                $scope.resetForm();
                showToast(
                    'Danh mục đã được cập nhật thành công',
                    'success',
                    'Cập nhật thành công!'
                );
            }, function(error) {
                showToast(
                    'Không thể cập nhật danh mục',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
    };

    // Edit category
    $scope.editCategory = function(category) {
        $scope.category = angular.copy(category);
        $scope.isEdit = true;
    };

    // Delete category
    $scope.deleteCategory = function(id) {
        if (confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
            $http.delete('/admin/api/categories/' + id)
                .then(function(response) {
                    $scope.getAllCategories();
                    showToast(
                        'Danh mục đã được xóa thành công',
                        'success',
                        'Xóa danh mục thành công!'
                    );
                }, function(error) {
                    showToast(
                        'Không thể xóa danh mục',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Reset form
    $scope.resetForm = function() {
        $scope.category = {};
        $scope.isEdit = false;
    };

    // Initialize data
    $scope.getAllCategories();
});

app.controller('AdminBrandsController', function($scope, $http) {
    $scope.brands = [];
    $scope.brand = {};
    $scope.isEdit = false;

    //  getAllBrands
    $scope.getAllBrands = function() {
        $http.get('/admin/api/brands').then(function(response) {
            // Cập nhật URL ảnh cho tất cả brands
            $scope.brands = response.data.map(brand => {
                brand.imageUrl = $scope.getImageUrl(brand.imageUrl);
                return brand;
            });
        }, function(error) {
            console.error('Error fetching brands', error);
        });
    };

    $scope.getImageUrl = function(imageUrl) {
        if (!imageUrl) return '';
        return imageUrl + '?t=' + new Date().getTime();
    };

    // Cập nhật các hàm xử lý để sử dụng alert thay vì showToast
    $scope.saveBrand = function() {
        // Kiểm tra tên thương hiệu trống
        if (!$scope.brand.name || $scope.brand.name.trim() === '') {
            showToast(
                'Vui Lòng nhập tên thương hiệu',
                'warning',
                'Thiếu thông tin!'
            );
            return;
        }

        // Kiểm tra nếu ảnh chưa được thêm
        var imageFile = document.getElementById('brandImage').files[0];
        if (!imageFile) {
            showToast(
                'Vui Lòng nhập thêm hình ảnh',
                'warning',
                'Thiếu thông tin!'
            );
            return;
        }
        var formData = new FormData();
        formData.append('name', $scope.brand.name);

        var imageFile = document.getElementById('brandImage').files[0];
        if (imageFile) {
            formData.append('image', imageFile);
        }

        $http.post('/admin/api/brands', formData, {
            transformRequest: angular.identity,
            headers: {
                'Content-Type': undefined
            }
        }).then(function(response) {
            var newBrand = response.data;
            if (newBrand.imageUrl) {
                newBrand.imageUrl = $scope.getImageUrl(newBrand.imageUrl);
            }
            $scope.brands.push(newBrand);
            $scope.resetForm();
            showToast(
                'Thương hiệu mới đã được thêm thành công',
                'success',
                'Thêm thương hiệu thành công!'
            );
        }, function(error) {
            showToast(
                'Không thể thêm thương hiệu mới',
                'error',
                'Lỗi hệ thống!'
            );
        });
    };

    $scope.updateBrand = function() {
        var formData = new FormData();
        formData.append('name', $scope.brand.name);

        var imageFile = document.getElementById('brandImage').files[0];
        if (imageFile) {
            formData.append('image', imageFile);
        }

        $http.put('/admin/api/brands/' + $scope.brand.id, formData, {
            transformRequest: angular.identity,
            headers: {
                'Content-Type': undefined
            }
        }).then(function(response) {
            var updatedBrand = response.data;
            if (updatedBrand.imageUrl) {
                updatedBrand.imageUrl = $scope.getImageUrl(updatedBrand.imageUrl);
            }
            var index = $scope.brands.findIndex(b => b.id === updatedBrand.id);
            if (index !== -1) {
                $scope.brands[index] = updatedBrand;
            }
            $scope.resetForm();
            showToast(
                'Thương hiệu đã được cập nhật thành công',
                'success',
                'Cập nhật thành công!'
            );
        }, function(error) {
            showToast(
                'Không thể cập nhật thương hiệu',
                'error',
                'Lỗi hệ thống!'
            );
        });
    };


    // Edit brand
    $scope.editBrand = function(brand) {
        $scope.brand = angular.copy(brand);
        $scope.isEdit = true;
    };

    // Delete brand
    $scope.deleteBrand = function(id) {
        if (confirm('Bạn có chắc chắn muốn xóa thương hiệu này?')) {
            $http.delete('/admin/api/brands/' + id)
                .then(function(response) {
                    $scope.getAllBrands();
                    showToast(
                        'Thương hiệu đã được xóa thành công',
                        'success',
                        'Xóa thương hiệu thành công!'
                    );
                }, function(error) {
                    showToast(
                        'Không thể xóa thương hiệu',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Reset form
    $scope.resetForm = function() {
        $scope.brand = {};
        $scope.isEdit = false;

        var fileInput = document.getElementById('brandImage');
        if (fileInput) {
            fileInput.value = '';
        }
    };

    // Initialize data
    $scope.getAllBrands();
});

app.controller('AdminPromotionsController', function($scope, $http) {
    $scope.promotions = [];
    $scope.promotion = {};

    // Hàm tải tất cả mã khuyến mãi
    $scope.loadPromotions = function() {
        $http.get('/admin/api/promotions')
            .then(function(response) {
                $scope.promotions = response.data;
            });
    };

    // Tải dữ liệu khuyến mãi khi trang tải
    $scope.loadPromotions();

    // Hàm lưu hoặc cập nhật khuyến mãi
    $scope.savePromotion = function() {
        if ($scope.promotion.id) {
            // Cập nhật khuyến mãi
            $http.put(`/admin/api/promotions/${$scope.promotion.id}`, $scope.promotion)
                .then(function() {
                    $scope.loadPromotions();
                    $scope.resetForm();
                    showToast(
                        'Khuyến mãi đã được cập nhật thành công',
                        'success',
                        'Cập nhật thành công!'
                    );
                }, function(error) {
                    showToast(
                        'Không thể cập nhật khuyến mãi',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        } else {
            // Thêm mới khuyến mãi
            $http.post('/admin/api/promotions', $scope.promotion)
                .then(function() {
                    $scope.loadPromotions();
                    $scope.resetForm();
                    showToast(
                        'Khuyến mãi mới đã được thêm thành công',
                        'success',
                        'Thêm khuyến mãi thành công!'
                    );
                }, function(error) {
                    showToast(
                        'Không thể thêm khuyến mãi mới',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Hàm chỉnh sửa khuyến mãi
    $scope.editPromotion = function(promotion) {
        $scope.promotion = angular.copy(promotion);
        $scope.promotion.startDate = new Date(promotion.startDate);
        $scope.promotion.endDate = new Date(promotion.endDate);
    };

    // Hàm xóa khuyến mãi
    $scope.deletePromotion = function(id) {
        if (confirm('Bạn có chắc chắn muốn xóa khuyến mãi này?')) {
            $http.delete(`/admin/api/promotions/${id}`)
                .then(function() {
                    $scope.loadPromotions();
                    showToast(
                        'Khuyến mãi đã được xóa thành công',
                        'success',
                        'Xóa khuyến mãi thành công!'
                    );
                }, function(error) {
                    showToast(
                        'Không thể xóa khuyến mãi',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Hàm kiểm tra khuyến mãi có đang hoạt động hay không
    $scope.isActive = function(promotion) {
        const now = new Date();
        const startDate = new Date(promotion.startDate);
        const endDate = new Date(promotion.endDate);
        return now >= startDate && now <= endDate;
    };

    // Hàm reset form
    $scope.resetForm = function() {
        $scope.promotion = {};
        $scope.promotionForm.$setPristine(); // Đặt form về trạng thái nguyên vẹn
    };
});

app.controller('AdminStatisticsController', function($scope, $http) {
    const currentYear = new Date().getFullYear();

    // Lấy dữ liệu và vẽ biểu đồ doanh thu
    $http.get(`/admin/api/statistics/monthly-revenue?year=` + currentYear).then(function(response) {
        renderRevenueChart(response.data);
    });

    // Lấy dữ liệu và vẽ biểu đồ số lượng đơn hàng
    $http.get(`/admin/api/statistics/monthly-order-count?year=` + currentYear).then(function(response) {
        renderOrderChart(response.data);
    });

    // Lấy dữ liệu và vẽ biểu đồ số lượng sản phẩm đã bán
    $http.get(`/admin/api/statistics/monthly-product-sold?year=` + currentYear).then(function(response) {
        renderProductSoldChart(response.data);
    });

    // Vẽ biểu đồ doanh thu
    function renderRevenueChart(revenueData) {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        const months = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
        const revenues = Array(12).fill(0);

        revenueData.forEach(item => {
            revenues[item.month - 1] = item.totalRevenue;
        });

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: 'Doanh thu (VND)',
                    data: revenues,
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    // Vẽ biểu đồ số lượng đơn hàng
    function renderOrderChart(orderData) {
        const ctx = document.getElementById('orderChart').getContext('2d');
        const months = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
        const orderCounts = Array(12).fill(0);

        orderData.forEach(item => {
            orderCounts[item.month - 1] = item.orderCount;
        });

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: 'Số lượng đơn hàng',
                    data: orderCounts,
                    backgroundColor: 'rgba(153, 102, 255, 0.2)',
                    borderColor: 'rgba(153, 102, 255, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    // Vẽ biểu đồ số lượng sản phẩm đã bán
    function renderProductSoldChart(productSoldData) {
        const ctx = document.getElementById('productSoldChart').getContext('2d');
        const months = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
        const productSoldCounts = Array(12).fill(0);

        productSoldData.forEach(item => {
            productSoldCounts[item.month - 1] = item.productSoldCount;
        });

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: 'Số lượng sản phẩm đã bán',
                    data: productSoldCounts,
                    backgroundColor: 'rgba(255, 159, 64, 0.2)',
                    borderColor: 'rgba(255, 159, 64, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    $scope.startDate = null;
    $scope.endDate = null;
    $scope.dateRangeRevenue = null;

    $scope.getRevenueByDateRange = function() {
        if (!$scope.startDate || !$scope.endDate) {
            alert('Vui lòng chọn ngày bắt đầu và ngày kết thúc');
            return;
        }

        // Chuyển đổi sang múi giờ Việt Nam
        const startDate = new Date($scope.startDate);
        const endDate = new Date($scope.endDate);

        // Format dates as YYYY-MM-DD
        const formattedStartDate = startDate.toLocaleDateString('en-CA'); // Format: YYYY-MM-DD
        const formattedEndDate = endDate.toLocaleDateString('en-CA');     // Format: YYYY-MM-DD

        $http.get('/admin/api/statistics/revenue-by-date', {
            params: {
                startDate: formattedStartDate,
                endDate: formattedEndDate
            }
        })
            .then(function(response) {
                $scope.dateRangeRevenue = response.data;
            })
            .catch(function(error) {
                console.error('Error fetching revenue:', error);
                alert('Có lỗi xảy ra khi lấy dữ liệu doanh thu');
            });
    };

    $scope.topProducts = [];
    $scope.topLimit = null; // Không set giá trị mặc định

    $scope.loadTopProducts = function() {
        if (!$scope.topLimit || $scope.topLimit < 1) {
            alert('Vui lòng nhập số lượng hợp lệ (lớn hơn 0)');
            return;
        }

        $http.get('/admin/api/statistics/top-purchased-products', {
            params: {
                limit: $scope.topLimit
            }
        })
            .then(function(response) {
                $scope.topProducts = response.data;
            })
            .catch(function(error) {
                console.error('Error fetching top products:', error);
                alert('Có lỗi xảy ra khi lấy dữ liệu sản phẩm');
            });
    };
});

app.controller("AdminOrdersController", function($scope, $http) {
    $scope.orders = [];
    $scope.selectedOrder = null;
    $scope.selectedStatus = 'all'; // Thêm biến theo dõi status đang được chọn
    $scope.searchQuery = '';
    $scope.currentPage = 1;
    $scope.itemsPerPage = 10;
    $scope.dateFilter = null;
    $scope.monthFilter = null;
    $scope.dateRange = {
        start: null,
        end: null
    };

    // Hàm format ngày theo định dạng Việt Nam
    function formatDateVN(date) {
        const d = new Date(date);
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        return `${day}-${month}-${year}`;
    }

    // Hàm lọc theo ngày
    $scope.filterByDate = function() {
        if ($scope.dateFilter) {
            const formattedDate = formatDateVN($scope.dateFilter);

            $http.get(`http://localhost:8080/admin/api/orders/date/${formattedDate}`)
                .then(function(response) {
                    $scope.orders = response.data;
                    $scope.currentPage = 1;
                })
                .catch(function(error) {
                    console.error('Error fetching orders by date:', error);
                    showToast(
                        'Không thể lọc đơn hàng theo ngày',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Hàm lọc theo tháng
    $scope.filterByMonth = function() {
        if ($scope.monthFilter) {
            const date = new Date($scope.monthFilter);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');

            $http.get(`http://localhost:8080/admin/api/orders/month/${year}/${month}`)
                .then(function(response) {
                    $scope.orders = response.data;
                    $scope.currentPage = 1;
                })
                .catch(function(error) {
                    console.error('Error fetching orders by month:', error);
                    showToast(
                        'Không thể lọc đơn hàng theo tháng',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    $scope.filterByDateRange = function() {
        if ($scope.dateRange.start && $scope.dateRange.end) {
            if (new Date($scope.dateRange.end) < new Date($scope.dateRange.start)) {
                showToast(
                    'Ngày kết thúc không được nhỏ hơn ngày bắt đầu',
                    'error',
                    'Lỗi!'
                );
                return;
            }
            const startDate = formatDateVN($scope.dateRange.start);
            const endDate = formatDateVN($scope.dateRange.end);

            $http.get(`http://localhost:8080/admin/api/orders/range/${startDate}/${endDate}`)
                .then(function(response) {
                    $scope.orders = response.data;
                    $scope.currentPage = 1;
                })
                .catch(function(error) {
                    console.error('Error fetching orders by date range:', error);
                    showToast(
                        'Không thể lọc đơn hàng theo khoảng thời gian',
                        'error',
                        'Lỗi hệ thống!'
                    );
                });
        }
    };

    // Format hiển thị ngày trong bảng
    $scope.formatDate = function(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
    };

    // Hàm xóa bộ lọc thời gian
    $scope.clearDateFilters = function() {
        $scope.dateFilter = null;
        $scope.monthFilter = null;
        $scope.dateRange = {
            start: null,
            end: null
        };
        $scope.getOrders($scope.selectedStatus);
    };


    // Hàm tìm kiếm và lọc đơn hàng
    $scope.filterOrders = function() {
        let filteredOrders = $scope.orders;

        if ($scope.searchQuery) {
            console.log('Search Query:', $scope.searchQuery);
            filteredOrders = filteredOrders.filter(order => {
                const matchId = order.id.toString().toLowerCase().includes($scope.searchQuery.toLowerCase());
                const matchUsername = order.user.username.toLowerCase().includes($scope.searchQuery.toLowerCase());
                const matchRecipient = order.recipientName.toLowerCase().includes($scope.searchQuery.toLowerCase());

                return matchId || matchUsername || matchRecipient;
            });
        }

        return filteredOrders;
    };
    // Hàm phân trang
    $scope.paginatedOrders = function() {
        let filtered = $scope.filterOrders();
        $scope.totalPages = Math.ceil(filtered.length / $scope.itemsPerPage);

        let begin = (($scope.currentPage - 1) * $scope.itemsPerPage);
        let end = begin + $scope.itemsPerPage;

        return filtered.slice(begin, end);
    };

    // Các hàm pagination
    $scope.changePage = function(page) {
        if (page >= 1 && page <= $scope.totalPages) {
            $scope.currentPage = page;
        }
    };

    $scope.getPages = function() {
        let pages = [];
        let totalPages = Math.ceil($scope.filterOrders().length / $scope.itemsPerPage);
        for (let i = 1; i <= totalPages; i++) {
            pages.push(i);
        }
        return pages;
    };

    // Cập nhật hàm lấy danh sách đơn hàng
    $scope.getOrders = function(status) {
        let url = 'http://localhost:8080/admin/api/orders';
        if (status && status !== 'all') {
            url += '/status/' + status;
        }

        $http.get(url)
            .then(function(response) {
                $scope.orders = response.data;
                $scope.currentPage = 1; // Reset về trang đầu khi load dữ liệu mới
            })
            .catch(function(error) {
                console.error('Error fetching orders:', error);
            });
    };

    // Thêm hàm lọc theo status
    $scope.filterByStatus = function(status) {
        $scope.selectedStatus = status;
        $scope.getOrders(status);
    };

    $scope.openOrderDetails = function(order) {
        console.log(order);
        $scope.selectedOrder = order;
        if (!order.orderItems) {
            $http.get('http://localhost:8080/admin/api/orders/' + order.id)
                .then(function(response) {
                    console.log(response.data);
                    order.orderItems = response.data.orderItems;
                    order.payment = response.data.payment;
                    $scope.selectedOrder = order;
                    var modal = new bootstrap.Modal(document.getElementById('orderDetailsModal'));
                    modal.show();
                })
                .catch(function(error) {
                    console.error('Error fetching order details:', error);
                });
        } else {
            var modal = new bootstrap.Modal(document.getElementById('orderDetailsModal'));
            modal.show();
        }
    };

    $scope.updateOrderStatus = function(orderId, status) {
        $http.put('http://localhost:8080/admin/api/orders/' + orderId + '/status', { status: status })
            .then(function(response) {
                console.log('Order status updated to ' + status + ':', response.data);
                if ($scope.selectedOrder && $scope.selectedOrder.id === orderId) {
                    $scope.selectedOrder.status = status;
                }

                const updatedOrder = $scope.orders.find(o => o.id === orderId);
                if (updatedOrder) {
                    updatedOrder.status = status;
                }

                // Sau khi cập nhật, refresh lại danh sách theo filter hiện tại
                $scope.getOrders($scope.selectedStatus);

                var modal = bootstrap.Modal.getInstance(document.getElementById('orderDetailsModal'));
                modal.hide();
                showToast(
                    'Trạng thái đơn hàng đã được cập nhật thành công',
                    'success',
                    'Cập nhật thành công!'
                );
            })
            .catch(function(error) {
                showToast(
                    'Không thể cập nhật trạng thái đơn hàng',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
    };

    // Khởi tạo với tất cả đơn hàng
    $scope.getOrders('all');
});

app.controller('AdminUsersController', function ($scope, $http) {
    $scope.users = [];
    $scope.availableRoles = ['ROLE_USER','ROLE_STAFF', 'ROLE_ADMIN'];
    $scope.selectedUser = {};

    // Lấy danh sách người dùng
    function fetchUsers() {
        $http.get('/api/users').then(function (response) {
            $scope.users = response.data;
        });
    }
    // Khởi tạo dữ liệu
    fetchUsers();

    $scope.getRoleNames = function(user) {
        return user.roles.map(function(role) {
            return role.name;
        }).join(', ');
    };


    // Mở modal chỉnh sửa vai trò
    $scope.openEditRoleModal = function (user) {
        $scope.selectedUser = angular.copy(user);
        const roleModal = new bootstrap.Modal(document.getElementById('roleModal'));
        roleModal.show();
    };

    // Lưu thay đổi vai trò
    $scope.saveRoles = function () {
        const userId = $scope.selectedUser.id;
        const selectedRoles = $scope.selectedUser.roles;
        $http.put(`/api/users/${userId}/roles`, selectedRoles).then(function () {
            showToast(
                'Vai trò người dùng đã được cập nhật thành công',
                'success',
                'Cập nhật thành công!'
            );
            fetchUsers();
            bootstrap.Modal.getInstance(document.getElementById('roleModal')).hide();
        }, function (error) {
            showToast(
                'Không thể cập nhật vai trò người dùng',
                'error',
                'Lỗi hệ thống!'
            );
        });
    };

    $scope.deleteUser = function (userId) {
        if (confirm('Bạn có chắc chắn muốn xóa người dùng này?')) {
            $http.delete(`/api/users/${userId}`).then(function (response) {
                // Hiển thị thông báo thành công
                showToast(
                    'Người dùng đã được xóa thành công',
                    'success',
                    'Xóa người dùng thành công!'
                );

                // Loại bỏ người dùng khỏi danh sách hiện tại
                $scope.users = $scope.users.filter(function(user) {
                    return user.id !== userId;
                });
            }, function(error) {
                showToast(
                    'Không thể xóa người dùng',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
        }
    };

});