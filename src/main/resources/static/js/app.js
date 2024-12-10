// app.js
var app = angular.module('myApp', ['ngRoute']);

app.config(function($routeProvider) {
    $routeProvider
        .when('/home', {
            templateUrl: 'views/home.html',
            controller: 'HomeController'
        })
        .when('/about', {
            templateUrl: 'views/about.html'
        })
        .when('/news', {
            templateUrl: 'views/news.html'
        })
        .when('/contact', {
            templateUrl: 'views/contact.html'
        })
        .when('/cart', {
            templateUrl: 'views/cart.html',
            controller: 'CartController'
        })
        .when('/shop', {
            templateUrl: 'views/shop.html',
            controller: 'ShopController',
        })
        .when('/product-details/:productId', {
            templateUrl: 'views/product-details.html',
            controller: 'ProductDetailsController'
        })
        .when('/order', {
            templateUrl: 'views/order.html',
            controller: 'OrderController'
        })
        .when('/order-history', {
            templateUrl: 'views/order-history.html',
            controller: 'OrderHistoryController'
        })
        .when('/address', {
            templateUrl:'views/address.html',
            controller:'AddressController'
        })
        .otherwise({
            redirectTo: '/home'
        });
});



app.controller('HomeController', function($scope, $http) {
    $scope.mechanicalWatch = [];
    $scope.smartWatches = [];
    $scope.isLoggedIn = false;

    // Lấy sản phẩm điện thoại
    $http.get('/api/products/mechanicalWatch')
        .then(function(response) {
            $scope.mechanicalWatch = response.data;
        }, function(error) {
            console.error('Lỗi khi lấy dữ liệu sản phẩm điện thoại:', error);
        });

    // Lấy sản phẩm đồng hồ
    $http.get('/api/products/smartWatches')
        .then(function(response) {
            $scope.smartWatches = response.data;
        }, function(error) {
            console.error('Lỗi khi lấy dữ liệu sản phẩm đồng hồ:', error);
        });

    // Kiểm tra trạng thái đăng nhập
    $http.get('/api/auth/status')
        .then(function(response) {
            $scope.isLoggedIn = response.data.isLoggedIn;
        }, function(error) {
            console.error('Lỗi khi kiểm tra trạng thái đăng nhập:', error);
        });

});


app.controller('ShopController', function($scope, $http) {
    $scope.products = [];
    $scope.categories = [];
    $scope.brands = [];
    $scope.searchText = '';
    $scope.selectedCategory = '';
    $scope.selectedBrand = null;
    $scope.minPrice = '';
    $scope.maxPrice = '';
    $scope.currentPage = 1;
    $scope.pageSize = 12;

    $scope.getCategories = function() {
        $http.get('/api/products/categories')
            .then(function(response) {
                $scope.categories = response.data;
            })
            .catch(function(error) {
                console.error('Error fetching categories:', error);
            });
    };

    $scope.getBrands = function() {
        $http.get('/api/products/brands')
            .then(function(response) {
                $scope.brands = response.data;
            })
            .catch(function(error) {
                console.error('Error fetching categories:', error);
            });
    };


    // Lấy tất cả sản phẩm từ API
    $scope.getProducts = function() {
        $http.get('/api/products')
            .then(function(response) {
                $scope.products = response.data;

                // Thiết lập quantity mặc định cho mỗi sản phẩm
                $scope.products.forEach(function(product) {
                    product.quantity = 1;
                });
            })
            .catch(function(error) {
                console.error('Error fetching products:', error);
            });
    };

    // Thêm sản phẩm vào giỏ hàng
    $scope.addToCart = function(product) {
        $http.get('/api/auth/status')
            .then(function(response) {
                if (response.data.isLoggedIn) {
                    const data = {
                        productId: product.id,
                        quantity: product.quantity || 1
                    };

                    $http.post('/api/cart/add', data)
                        .then(function(response) {
                            showToast(
                                'Sản phẩm đã được thêm vào giỏ hàng',
                                'success',
                                'Thêm vào giỏ hàng thành công!'
                            );
                        })
                        .catch(function(error) {
                            showToast(
                                'Vui lòng thử lại sau',
                                'error',
                                'Không thể thêm vào giỏ hàng!'
                            );
                        });
                } else {
                    showToast(
                        'Vui lòng đăng nhập để tiếp tục',
                        'warning',
                        'Bạn chưa đăng nhập!'
                    );
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                }
            });
    };

    $scope.filterByBrand = function(brandId) {
        if ($scope.selectedBrand === brandId) {
            $scope.selectedBrand = null;
        } else {
            $scope.selectedBrand = brandId;
        }
        $scope.currentPage = 1;
    };

    // Custom filter function
    $scope.searchFilter = function(product) {
        const matchesSearchText = !$scope.searchText || product.name.toLowerCase().includes($scope.searchText.toLowerCase());
        const matchesCategory = !$scope.selectedCategory || product.category.id === Number($scope.selectedCategory); // Chuyển đổi thành số
        const matchesMinPrice = !$scope.minPrice || product.price >= $scope.minPrice;
        const matchesMaxPrice = !$scope.maxPrice || product.price <= $scope.maxPrice;
        const matchesBrand = !$scope.selectedBrand || product.brand.id === $scope.selectedBrand;

        return matchesSearchText && matchesCategory && matchesMinPrice && matchesMaxPrice && matchesBrand;
    };


    // Paginated products
    $scope.paginatedProducts = function() {
        const filtered = $scope.products.filter($scope.searchFilter);
        const begin = ($scope.currentPage - 1) * $scope.pageSize;
        return filtered.slice(begin, begin + $scope.pageSize);
    };

    // Total pages
    $scope.totalPages = function() {
        const filtered = $scope.products.filter($scope.searchFilter);
        return Array.from({ length: Math.ceil(filtered.length / $scope.pageSize) }, (_, i) => i + 1);
    };

    // Change page
    $scope.changePage = function(page) {
        if (page >= 1 && page <= $scope.totalPages().length) {
            $scope.currentPage = page;
        }
    };

    $scope.getProducts();
    $scope.getCategories();
    $scope.getBrands();
});


app.controller('ProductDetailsController', function($scope, $http, $routeParams) {
    var productId = $routeParams.productId;  // Lấy productId từ URL
    $scope.ratingStats = {};
    $scope.totalRatings = 0;
    $scope.averageRating = 0;
    $scope.currentPage = 0; // Current page for comments
    $scope.pageSize = 3; // Number of comments per page
    $scope.totalComments = 0; // Total number of comments

    // Gọi API để lấy thông tin chi tiết sản phẩm
    $http.get('/api/products/' + productId)
        .then(function(response) {
            $scope.product = response.data;  // Gán dữ liệu sản phẩm vào scope

            // Sau khi lấy được thông tin sản phẩm, lấy categoryId để lấy các sản phẩm cùng loại
            var categoryId = $scope.product.category.id; // Lấy categoryId từ product
            if (categoryId) {
                return $http.get('/api/products/category/' + categoryId);
            } else {
                console.error('categoryId không hợp lệ:', categoryId);
                return Promise.reject('categoryId không hợp lệ');
            }
        })
        .then(function(response) {
            if (response && response.data) {
                $scope.relatedProducts = response.data.filter(function(item) {
                    return item.id !== $scope.product.id; // Loại bỏ sản phẩm hiện tại
                });
            }
        })
        .catch(function(error) {
            console.error('Lỗi khi lấy dữ liệu chi tiết sản phẩm hoặc sản phẩm cùng loại:', error);
        });

    $scope.addToCart = function(product) {
        $http.get('/api/auth/status')
            .then(function(response) {
                if (response.data.isLoggedIn) {
                    const data = {
                        productId: product.id,
                        quantity: product.quantity || 1
                    };

                    $http.post('/api/cart/add', data)
                        .then(function(response) {
                            showToast(
                                'Sản phẩm đã được thêm vào giỏ hàng',
                                'success',
                                'Thêm vào giỏ hàng thành công!'
                            );
                        })
                        .catch(function(error) {
                            showToast(
                                'Vui lòng thử lại sau',
                                'error',
                                'Không thể thêm vào giỏ hàng!'
                            );
                        });
                } else {
                    showToast(
                        'Vui lòng đăng nhập để tiếp tục',
                        'warning',
                        'Bạn chưa đăng nhập!'
                    );
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                }
            });
    };

    // Lấy thống kê đánh giá theo sản phẩm
    $http.get('/api/comments/' + productId + '/ratings')
        .then(function(response) {
            $scope.ratingStats = response.data;
            // Tính tổng số đánh giá
            $scope.totalRatings = Object.values($scope.ratingStats).reduce((a, b) => a + b, 0);
            // Tính điểm trung bình
            if ($scope.totalRatings > 0) {
                $scope.averageRating = Object.entries($scope.ratingStats)
                    .reduce((acc, [rating, count]) => acc + (rating * count), 0) / $scope.totalRatings;
            }
            console.log('Rating stats:', $scope.ratingStats);
        })
        .catch(function(error) {
            console.error('Lỗi khi lấy thống kê đánh giá:', error);
            showToast(
                'Không thể tải thông tin đánh giá',
                'error',
                'Lỗi!'
            );
        });

    // Lấy thông tin người dùng hiện tại
    $scope.currentUserId = null;
    $http.get('/api/auth/current-user') // API trả về thông tin user hiện tại
        .then(function(response) {
            $scope.currentUserId = response.data.id; // Lưu userId hiện tại
            $scope.getComments();
        });

    // Lấy danh sách bình luận cho sản phẩm hiện tại
    $scope.getComments = function () {
        $http.get(`/api/comments/product/${productId}?page=${$scope.currentPage}&size=${$scope.pageSize}`)
            .then(function (response) {
                $scope.comments = response.data.content.map(comment => {
                    // Create image URL if available
                    if (comment.commentImages && comment.commentImages.length > 0) {
                        comment.commentImages = comment.commentImages.map(image => $scope.getImageUrl(image.imageUrl));
                    }
                    // Determine if the comment belongs to the current user
                    comment.isOwner = (comment.user.id === $scope.currentUserId);
                    return comment;
                });
                $scope.totalComments = response.data.totalElements;  // Tổng số bình luận
                $scope.totalPages = response.data.totalPages;  // Tổng số trang
                $scope.pageSize = response.data.size;  // Kích thước trang
            })
            .catch(function (error) {
                console.error('Error fetching comments:', error);
            });
    };

    // Function to go to the next page
    $scope.nextPage = function () {
        if ($scope.currentPage < Math.ceil($scope.totalComments / $scope.pageSize) - 1) {
            $scope.currentPage++;
            $scope.getComments(); // Fetch comments for the new page
        }
    };

// Function to go to the previous page
    $scope.prevPage = function () {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
            $scope.getComments(); // Fetch comments for the new page
        }
    };
// Gọi hàm lấy danh sách bình luận khi khởi tạo controller
    $scope.getComments();

// Thêm bình luận mới
    $scope.addComment = function () {
        $http.get('/api/auth/status') // Kiểm tra trạng thái đăng nhập của người dùng
            .then(function(response) {
                if (!response.data.isLoggedIn) {
                    // Nếu người dùng chưa đăng nhập, hiển thị thông báo và chuyển hướng đến trang login
                    showToast(
                        'Vui lòng đăng nhập để thêm bình luận',
                        'warning',
                        'Bạn chưa đăng nhập!'
                    );
                    setTimeout(() => {
                        window.location.href = '/login'; // Chuyển hướng đến trang đăng nhập
                    }, 2000);
                    return; // Dừng lại không thực hiện thêm bình luận nếu chưa đăng nhập
                }

                // Nếu đã đăng nhập, tiếp tục xử lý thêm bình luận
                if (!$scope.commentText) {
                    showToast(
                        'Vui lòng nhập nội dung bình luận',
                        'warning',
                        'Thiếu thông tin!'
                    );
                    return;
                }
                if (!$scope.rating) {
                    showToast(
                        'Vui lòng chọn số sao đánh giá',
                        'warning',
                        'Thiếu đánh giá!'
                    );
                    return;
                }

                var formData = new FormData();
                formData.append('productId', productId);
                formData.append('commentText', $scope.commentText);
                formData.append('rating', $scope.rating);

                var imageFiles = document.getElementById('commentImages').files;
                if (imageFiles.length > 0) {
                    Array.from(imageFiles).forEach(file => {
                        formData.append('images', file);
                    });
                }

                $http.post('/api/comments', formData, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                }).then(function (response) {
                    var newComment = response.data;

                    // Tạo URL ảnh nếu có
                    if (newComment.commentImages && newComment.commentImages.length > 0) {
                        newComment.commentImages = newComment.commentImages.map(image => $scope.getImageUrl(image.imageUrl));
                    }

                    $scope.comments.unshift(newComment);
                    $scope.resetCommentForm();
                    showToast(
                        'Bình luận của bạn đã được thêm thành công',
                        'success',
                        'Thành công!'
                    );
                }).catch(function (error) {
                    showToast(
                        'Không thể thêm bình luận. Vui lòng thử lại',
                        'error',
                        'Lỗi!'
                    );
                    console.error('Lỗi khi thêm bình luận:', error);
                });
            })
            .catch(function(error) {
                console.error('Lỗi khi kiểm tra trạng thái đăng nhập:', error);
            });
    };


// Reset form bình luận
    $scope.resetCommentForm = function () {
        $scope.commentText = '';
        $scope.rating = 0;
        $scope.imagePreview = [];
        document.getElementById('commentImages').value = null;
    };

// Xóa bình luận
    $scope.deleteComment = function (commentId) {
        if (confirm('Bạn có chắc chắn muốn xóa bình luận này?')) {
            $http.delete('/api/comments/' + commentId)
                .then(function () {
                    $scope.comments = $scope.comments.filter(comment => comment.id !== commentId);
                    showToast(
                        'Bình luận đã được xóa thành công',
                        'success',
                        'Đã xóa!'
                    );
                })
                .catch(function (error) {
                    showToast(
                        'Không thể xóa bình luận. Vui lòng thử lại',
                        'error',
                        'Lỗi!'
                    );
                    console.error('Lỗi khi xóa bình luận:', error);
                });
        }
    };

    // Thêm các phương thức xử lý modal
    $scope.isModalOpen = false;
    $scope.fullImageUrl = '';
    $scope.modalImages = [];
    $scope.currentImageIndex = 0;

    // Cập nhật hàm showFullImage để xử lý cả trường hợp một ảnh và nhiều ảnh
    $scope.showFullImage = function(imageUrl, images) {
        // Nếu images là một mảng, sử dụng nó
        if (Array.isArray(images)) {
            $scope.modalImages = images;
        }
        // Nếu images là một object có thuộc tính commentImages
        else if (images && images.commentImages) {
            $scope.modalImages = images.commentImages;
        }
        // Nếu chỉ có một ảnh
        else {
            $scope.modalImages = [imageUrl];
        }

        $scope.currentImageIndex = $scope.modalImages.indexOf(imageUrl);
        $scope.fullImageUrl = '/imageUrl/' + imageUrl;
        $scope.isModalOpen = true;
        document.body.style.overflow = 'hidden';
    };

    // Thêm các hàm điều hướng
    $scope.nextImage = function() {
        if ($scope.currentImageIndex < $scope.modalImages.length - 1) {
            $scope.currentImageIndex++;
            $scope.fullImageUrl = '/imageUrl/' + $scope.modalImages[$scope.currentImageIndex];
        }
    };

    $scope.prevImage = function() {
        if ($scope.currentImageIndex > 0) {
            $scope.currentImageIndex--;
            $scope.fullImageUrl = '/imageUrl/' + $scope.modalImages[$scope.currentImageIndex];
        }
    };

    // Cập nhật hàm closeModal
    $scope.closeModal = function() {
        $scope.isModalOpen = false;
        $scope.fullImageUrl = '';
        $scope.modalImages = [];
        $scope.currentImageIndex = 0;
        document.body.style.overflow = 'auto';
    };


// Thêm event listener để đóng modal khi nhấn ESC
    angular.element(document).on('keyup', function(e) {
        if ($scope.isModalOpen) {
            $scope.$apply(function() {
                switch(e.key) {
                    case 'ArrowLeft':
                        $scope.prevImage();
                        break;
                    case 'ArrowRight':
                        $scope.nextImage();
                        break;
                    case 'Escape':
                        $scope.closeModal();
                        break;
                }
            });
        }
    });

// Hiển thị URL ảnh bình luận
    $scope.getImageUrl = function (imageUrl) {
        if (!imageUrl) return '';
        return imageUrl + '?t=' + new Date().getTime(); // Đường dẫn gốc tới thư mục tải lên
    };

// Xử lý hình ảnh preview
    $scope.imagePreview = [];
    document.getElementById('commentImages').addEventListener('change', function (event) {
        const files = event.target.files;
        $scope.imagePreview = [];
        Array.from(files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function (e) {
                $scope.$apply(() => {
                    $scope.imagePreview.push(e.target.result);
                });
            };
            reader.readAsDataURL(file);
        });
    });

// Hàm xử lý sự kiện xóa preview ảnh
    $scope.removeImage = function (index) {
        $scope.imagePreview.splice(index, 1); // Xóa ảnh tại vị trí cụ thể
        const imageInput = document.getElementById('commentImages');
        const dataTransfer = new DataTransfer();
        Array.from(imageInput.files).forEach((file, i) => {
            if (i !== index) dataTransfer.items.add(file);
        });
        imageInput.files = dataTransfer.files;
    };

    $scope.isCommentOwner = function(comment) {
        return comment.isOwner = (comment.user.id === $scope.currentUserId);;
    };

    // Chức năng sao đánh giá
    $scope.setRating = function(rating) {
        $scope.rating = rating;
    };

    $scope.currentIndex = 0;

    $scope.getSlidesToShow = function() {
        if (window.innerWidth <= 576) return 1;
        if (window.innerWidth <= 992) return 2;
        if (window.innerWidth <= 1200) return 3;
        return 4;
    };

    $scope.slidesToShow = $scope.getSlidesToShow();

    $scope.getSliderStyle = function() {
        const slideWidth = 100 / $scope.slidesToShow;
        return {
            transform: `translateX(-${$scope.currentIndex * slideWidth}%)`
        };
    };

    $scope.prevSlide = function() {
        if ($scope.currentIndex > 0) {
            $scope.currentIndex--;
        }
    };

    $scope.nextSlide = function() {
        const totalSlides = $scope.relatedProducts.length;
        if ($scope.currentIndex < totalSlides - $scope.slidesToShow) {
            $scope.currentIndex++;
        }
    };

    $scope.isPrevDisabled = function() {
        return $scope.currentIndex === 0;
    };

    $scope.isNextDisabled = function() {
        // Kiểm tra nếu $scope.relatedProducts là một mảng hợp lệ
        const totalSlides = Array.isArray($scope.relatedProducts) ? $scope.relatedProducts.length : 0;

        // Trả về kết quả dựa trên số lượng sản phẩm và số slide hiển thị
        return $scope.currentIndex >= totalSlides - $scope.slidesToShow;
    };


    // Cập nhật slidesToShow khi resize window
    angular.element(window).on('resize', function() {
        $scope.$apply(function() {
            $scope.slidesToShow = $scope.getSlidesToShow();
            $scope.currentIndex = 0;
        });
    });
});


app.controller('CartController', function($scope, $http) {
    $scope.cartItems = [];
    $scope.selectAll = false;

    $scope.loadUserInfo = function() {
        $http.get('/api/auth/current-user')
            .then(function(response) {
                if (response.data && response.data.id) {
                    // Lưu userId vào localStorage
                    localStorage.setItem('userId', response.data.id);
                }
            })
            .catch(function(error) {

            });
    };

    $scope.loadCart = function() {
        const userId = localStorage.getItem('userId');
        $http.get('/api/cart').then(function(response) {
            console.log(response.data);
            $scope.cartItems = response.data;

            // Lấy các sản phẩm đã chọn từ localStorage
            var savedSelectedItems = JSON.parse(localStorage.getItem(`selectedCartItems_${userId}`)) || [];

            $scope.cartItems.forEach(function(item) {
                item.selected = savedSelectedItems.some(savedItem => savedItem.id === item.id);
            });
            // Cập nhật trạng thái selected của mỗi sản phẩm
            $scope.cartItems.forEach(function(item) {
                item.selected = savedSelectedItems.some(function(selectedItem) {
                    return selectedItem.id === item.id;
                });
            });

            // Cập nhật lại danh sách selectedCartItems
            $scope.selectedCartItems = savedSelectedItems.map(item => item.id);
        }, function(error) {
            console.error('Error loading cart:', error);
        });
    };

// Hàm để cập nhật lựa chọn sản phẩm
    $scope.updateSelection = function() {
        const userId = localStorage.getItem('userId');


        // Lọc các sản phẩm được chọn
        const selectedItems = $scope.cartItems.filter(function(item) {
            return item.selected;
        });

        // Lưu danh sách sản phẩm đã chọn vào localStorage
        localStorage.setItem(`selectedCartItems_${userId}`, JSON.stringify(selectedItems));

        // Cập nhật danh sách ID của các sản phẩm đã chọn
        $scope.selectedCartItems = selectedItems.map(function(item) {
            return item.id;
        });

        // Kiểm tra nếu tất cả các sản phẩm đã được chọn
        $scope.selectAll = selectedItems.length === $scope.cartItems.length;
    };

// Hàm chọn/bỏ chọn tất cả sản phẩm
    $scope.toggleSelectAll = function() {
        // Cập nhật trạng thái tất cả sản phẩm dựa trên selectAll
        $scope.cartItems.forEach(function(item) {
            item.selected = $scope.selectAll;
        });
        console.log('Updated Cart Items:', $scope.cartItems);
        // Lưu lại vào localStorage
        $scope.updateSelection();
    };


    $scope.removeFromCart = function(cartId) {
        const userId = localStorage.getItem('userId');
        $http.delete('/api/cart/remove/' + cartId)
            .then(function(response) {
                if (response.data && typeof response.data === "object") {
                    alert(response.data.message || response.data.error);
                }

                // Cập nhật lại danh sách sản phẩm trong giỏ hàng
                $scope.cartItems = $scope.cartItems.filter(function(item) {
                    return item.id !== cartId;
                });

                // Cập nhật lại localStorage
                var selectedItems = $scope.cartItems.filter(function(item) {
                    return item.selected;
                });
                localStorage.setItem(`selectedCartItems_${userId}`, JSON.stringify(selectedItems));;

                // Cập nhật lại danh sách ID các sản phẩm đã chọn
                $scope.selectedCartItems = selectedItems.map(function(item) {
                    return item.id;
                });
            }, function(error) {
                console.error('Error removing item:', error);
                alert('Error removing item: ' + (error.data?.error || 'An error occurred'));
            });
    };




    // Function to update cart item quantity
    $scope.updateCart = function(cartId, quantity) {
        const userId = localStorage.getItem('userId');

        $http.put('/api/cart/update/' + cartId, null, { params: { quantity: quantity } })
            .then(function(response) {
                // Cập nhật số lượng sản phẩm trong mảng cartItems
                const item = $scope.cartItems.find(item => item.id === cartId);
                if (item) {
                    item.quantity = quantity; // Cập nhật số lượng cho item hiện tại
                }

                // Cập nhật lại localStorage sau khi thay đổi số lượng
                var updatedCartItems = $scope.cartItems.filter(function(item) {
                    return item.selected;
                });

                // Lưu danh sách sản phẩm đã chọn vào localStorage dưới khóa userId
                localStorage.setItem(`selectedCartItems_${userId}`, JSON.stringify(updatedCartItems));
                console.log('LocalStorage updated:', updatedCartItems);

            }, function(error) {
                console.error('Error updating item:', error);
                // Bạn có thể chọn để không hiển thị bất kỳ thông báo nào ở đây
            });
    };



    // Hàm tính tổng các sản phẩm đã chọn
    $scope.getTotal = function () {
        return $scope.cartItems
            .filter(item => item.selected)
            .reduce((total, item) => total + item.quantity * item.price, 0);
    };
    $scope.loadUserInfo();
    // Load the cart on page load
    $scope.loadCart();
});


app.controller('OrderController', function($scope, $http, $window) {
    $scope.cartItems = [];
    $scope.order = {
        totalPrice: 0,
        paymentMethod: '',
        promoCode: '',
        recipientName: '',
        phoneNumber: '',
        address: ''
    };
    $scope.orderMessage = '';
    $scope.orderSuccess = false;
    $scope.promoMessage = '';
    $scope.promoSuccess = false;
    $scope.discountAmount = 0;
    $scope.promoApplied = false;
    $scope.addressSuggestions = [];

    // Hàm gọi API tìm kiếm địa chỉ từ OpenStreetMap (Nominatim API)
    $scope.fetchAddressSuggestions = function(query) {
        if (query.length < 3) {
            $scope.addressSuggestions = []; // Dừng lại nếu chuỗi nhập vào quá ngắn
            return;
        }

        // Gọi Nominatim API của OpenStreetMap
        const apiUrl = `https://nominatim.openstreetmap.org/search?q=${query}&format=json&address details=1&limit=5`;

        $http.get(apiUrl).then(function(response) {
            if (response.data && response.data.length > 0) {
                // Cập nhật danh sách gợi ý địa chỉ
                $scope.addressSuggestions = response.data;
            } else {
                $scope.addressSuggestions = []; // Nếu không có kết quả, xóa gợi ý
            }
        }).catch(function(error) {
            console.error('Error fetching address suggestions:', error);
            $scope.addressSuggestions = [];
        });
    };

    // Khi người dùng chọn một địa chỉ từ danh sách gợi ý
    $scope.selectAddress = function(suggestion) {
        $scope.order.address = suggestion.display_name; // Chọn địa chỉ hiển thị
        $scope.addressSuggestions = []; // Đóng danh sách gợi ý
    };

    $scope.loadUserInfo = function() {
        $http.get('/api/auth/current-user')
            .then(function(response) {
                if (response.data && response.data.id) {
                    // Lưu userId vào localStorage
                    localStorage.setItem('userId', response.data.id);
                }
            })
            .catch(function(error) {
                console.error("Error fetching user info:", error);
            });
    };

    // Load cart items from API
    $scope.loadCart = function() {
        const userId = localStorage.getItem('userId');
        // Lấy các sản phẩm được chọn từ localStorage
        var selectedItems = JSON.parse(localStorage.getItem(`selectedCartItems_${userId}`) || '[]');

        if (selectedItems.length === 0) {
            // Nếu không có sản phẩm nào được chọn, chuyển về trang giỏ hàng
            $window.location.href = '#!/cart';
            return;
        }

        $scope.cartItems = selectedItems;
        $scope.getDefaultAddress();
    };

    // Get default address
    $scope.getDefaultAddress = function() {
        $http.get('/api/addresses/default')
            .then(function(response) {
                if (response.data) {
                    $scope.order.recipientName = response.data.recipientName;
                    $scope.order.phoneNumber = response.data.phoneNumber;
                    $scope.order.address = response.data.address;
                    $scope.isAddingAddress = false; // Đã có địa chỉ, không hiển thị form thêm địa chỉ
                } else {
                    $scope.isAddingAddress = true; // Không có địa chỉ, hiển thị form để thêm địa chỉ
                }
            })
            .catch(function(error) {
                console.error("Error fetching default address:", error);
            });
    };
    $scope.changeAddress = function() {
        $window.location.href = '#!/address';
    };

    // Save new address
    $scope.saveAddress = function() {
        const addressData = {
            recipientName: $scope.order.recipientName,
            phoneNumber: $scope.order.phoneNumber,
            address: $scope.order.address,
            isDefault: true // Đặt địa chỉ này là mặc định
        };

        $http.post('/api/addresses', addressData)
            .then(function(response) {
                showToast(
                    'Địa chỉ đã được lưu thành công',
                    'success',
                    'Lưu địa chỉ thành công!'
                );
                $scope.isAddingAddress = false;
                $scope.getDefaultAddress();
            })
            .catch(function(error) {
                showToast(
                    'Vui lòng thử lại sau',
                    'error',
                    'Không thể lưu địa chỉ!'
                );
            });
    };

    // Tính tạm tính (subtotal)
    $scope.getSubtotal = function() {
        return $scope.cartItems.reduce(function(total, item) {
            return total + (item.quantity * item.price);
        }, 0);
    };

    // Tính số tiền được giảm giá
    $scope.getDiscount = function() {
        return $scope.discountAmount || 0;
    };

    // Tính tổng cộng
    $scope.getTotal = function() {
        return $scope.getSubtotal() - $scope.getDiscount();
    };

    $scope.applyPromoCode = function() {
        var promoCode = $scope.order.promoCode;

        // Gửi yêu cầu kiểm tra mã khuyến mãi
        $http.post('/api/promo/check', {
            promoCode: promoCode
        }).then(function(response) {
            const data = response.data;

            if (data.valid) {
                let discountAmount = data.discountAmount || 0;
                let discountPercentage = data.discountPercentage || 0;

                // Tính toán giảm giá
                if (discountPercentage > 0) {
                    $scope.discountAmount = ($scope.getSubtotal() * discountPercentage / 100);
                } else {
                    $scope.discountAmount = discountAmount;
                }

                // Cập nhật trạng thái
                showToast(
                    'Áp dụng mã giảm giá thành công!',
                    'success',
                    'Thành công!'
                );
                $scope.promoMessage = 'Áp dụng mã giảm giá thành công!';
                $scope.promoSuccess = true;
                $scope.promoApplied = true;
            } else {
                // Mã không hợp lệ hoặc đã được sử dụng
                const message = data.message;
                showToast(
                    message,
                    'error',
                    'Lỗi!'
                );
                $scope.promoMessage = message;
                $scope.promoSuccess = false;
                $scope.discountAmount = 0;
            }
        }, function(error) {
            // Lỗi hệ thống
            showToast(
                'Vui lòng thử lại sau',
                'error',
                'Lỗi hệ thống!'
            );
            $scope.promoMessage = 'Lỗi khi áp dụng mã giảm giá!';
            $scope.promoSuccess = false;
            console.error('Error applying promo code:', error);
        });
    };


    // Place order với tổng tiền đã được tính toán chính xác
    $scope.placeOrder = function() {
        const userId = localStorage.getItem('userId');
        if (!$scope.order.recipientName || !$scope.order.phoneNumber || !$scope.order.address) {
            showToast(
                'Vui lòng nhập đầy đủ thông tin giao hàng!',
                'warning',
                'Thiếu thông tin!'
            );
            $scope.orderMessage = 'Vui lòng nhập đầy đủ thông tin giao hàng!';
            $scope.orderSuccess = false;
            document.getElementById('recipientInfo').focus();
            return;
        }

        if (!$scope.order.paymentMethod) {
            showToast(
                'Vui lòng chọn phương thức thanh toán!',
                'warning',
                'Thiếu thông tin!'
            );
            $scope.orderMessage = 'Vui lòng chọn phương thức thanh toán!';
            $scope.orderSuccess = false;
            document.getElementById('paymentMethod').scrollIntoView({ behavior: 'smooth' });
            return;
        }

        // Lấy danh sách sản phẩm được chọn từ localStorage
        var selectedItems = JSON.parse(localStorage.getItem(`selectedCartItems_${userId}`) || '[]');

        if (selectedItems.length === 0) {
            showToast(
                'Không có sản phẩm nào được chọn!',
                'warning',
                'Giỏ hàng trống!'
            );
            $scope.orderMessage = 'Không có sản phẩm nào được chọn!';
            $scope.orderSuccess = false;
            return;
        }

        var orderData = {
            selectedCartIds: selectedItems.map(item => item.id), // Chuyển đổi thành danh sách ID
            totalPrice: $scope.getTotal(),
            paymentMethod: $scope.order.paymentMethod,
            promoCode: $scope.promoApplied ? $scope.order.promoCode : null,
            recipientName: $scope.order.recipientName,
            phoneNumber: $scope.order.phoneNumber,
            address: $scope.order.address
        };

        console.log('Order Data:', orderData); // Để debug

        if ($scope.order.paymentMethod === 'VNPAY') {
            // Chuẩn bị dữ liệu thanh toán
            var orderData = {
                selectedCartIds: selectedItems.map(item => item.id),
                totalPrice: $scope.getTotal(),
                paymentMethod: 'VNPAY',
                recipientName: $scope.order.recipientName,
                phoneNumber: $scope.order.phoneNumber,
                address: $scope.order.address,
                promoCode: $scope.promoApplied ? $scope.order.promoCode : null
            };

            // Gửi request để lưu thông tin
            $http.post('/payment/prepare', orderData)
                .then(function(response) {
                    // Chuyển đến trang thanh toán VNPAY
                    window.location.href = '/payment/vnpay';
                })
                .catch(function(error) {
                    console.error('Error preparing payment:', error);
                    $scope.orderMessage = 'Lỗi khi chuẩn bị thanh toán!';
                    $scope.orderSuccess = false;
                });
            return;
        }

        $http.post('/api/order/place', orderData)
            .then(function(response) {
                showToast(
                    'Đơn hàng của bạn đã được đặt thành công!',
                    'success',
                    'Đặt hàng thành công!'
                );
                $scope.orderMessage = 'Đặt hàng thành công!';
                $scope.orderSuccess = true;

                // Xóa danh sách sản phẩm đã chọn khỏi localStorage
                localStorage.removeItem(`selectedCartItems_${localStorage.getItem('userId')}`);

                // Reset form và các trạng thái
                $scope.promoApplied = false;
                $scope.promoMessage = '';
                $scope.discountAmount = 0;
                $scope.order.promoCode = '';

                // Chuyển về trang giỏ hàng sau 2 giây
                setTimeout(function() {
                    $window.location.href = '#!/cart';
                }, 2000);
            })
            .catch(function(error) {
                $scope.orderMessage = error.data.error || 'Lỗi khi đặt hàng!';
                $scope.orderSuccess = false;
                console.error('Error placing order:', error);
            });
    };

    $scope.loadUserInfo();

    $scope.loadCart();
});


app.controller('OrderHistoryController', function($scope, $http, $window) {
    $scope.orders = [];
    $scope.selectedOrderDetails = null;

    $scope.orders.sort(function(a, b) {
        return new Date(b.orderDate) - new Date(a.orderDate); // So sánh ngày từ mới đến cũ
    });

    // Thêm hàm đặt lại đơn hàng
    $scope.reorder = function(order) {
        const userId = localStorage.getItem('userId');

        $http.get('/api/order/' + order.id).then(function(response) {
            const orderDetails = response.data;

            if (orderDetails.items && orderDetails.items.length > 0) {
                // Thêm từng sản phẩm vào giỏ hàng
                const addToCartPromises = orderDetails.items.map(item =>
                    $http.post('/api/cart/add', {
                        productId: item.productId,
                        quantity: item.quantity
                    })
                );

                Promise.all(addToCartPromises)
                    .then(() => {
                        // Sau khi thêm vào giỏ hàng, lấy giỏ hàng mới
                        return $http.get('/api/cart');
                    })
                    .then(cartResponse => {
                        // Lọc các sản phẩm vừa thêm vào và đánh dấu là selected
                        const newCartItems = cartResponse.data.filter(cartItem =>
                            orderDetails.items.some(orderItem =>
                                orderItem.productId === cartItem.productId
                            )
                        ).map(item => ({
                            ...item,
                            selected: true
                        }));

                        // Lưu vào localStorage
                        localStorage.setItem(`selectedCartItems_${userId}`, JSON.stringify(newCartItems));

                        // Chuyển đến trang đặt hàng
                        $window.location.href = '#!/cart';
                    })
                    .catch(error => {
                        console.error('Error processing reorder:', error);
                        showToast(
                            'Vui lòng thử lại sau',
                            'error',
                            'Lỗi xử lý đặt lại đơn hàng!'
                        );
                    });
            } else {
                showToast(
                    'Không tìm thấy sản phẩm nào trong đơn hàng',
                    'warning',
                    'Đơn hàng trống!'
                );
            }
        }, function(error) {
            console.error('Error getting order details:', error);
            showToast(
                'Không thể tải thông tin đơn hàng',
                'error',
                'Lỗi hệ thống!'
            );
        });
    };

    // Tải danh sách đơn hàng từ API
    $scope.loadOrderHistory = function() {
        $http.get('/api/order/history').then(function(response) {
            $scope.orders = response.data;
        }, function(error) {
            console.error('Error loading order history:', error);
        });
    };

    // Xem chi tiết đơn hàng
    $scope.viewOrderDetails = function(orderId) {
        $http.get('/api/order/' + orderId).then(function(response) {
            $scope.selectedOrderDetails = response.data;
        }, function(error) {
            console.error('Error loading order details:', error);
        });
    };

    // Hủy đơn hàng
    $scope.cancelOrder = function(orderId) {
        // Tạo modal HTML
        const modalHtml = `
        <div class="modal fade" id="cancelOrderModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Xác nhận hủy đơn hàng</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Vui lòng chọn lý do hủy đơn:</label>
                            <select id="cancelReason" class="form-select">
                                <option value="">-- Chọn lý do --</option>
                                <option value="CHANGE_MIND">Tôi muốn thay đổi sản phẩm</option>
                                <option value="WRONG_ADDRESS">Địa chỉ giao hàng không chính xác</option>
                                <option value="DUPLICATE_ORDER">Đặt trùng đơn hàng</option>
                                <option value="DELIVERY_TIME">Thời gian giao hàng không phù hợp</option>
                                <option value="PAYMENT_ISSUES">Vấn đề về thanh toán</option>
                                <option value="OTHER">Lý do khác</option>
                            </select>
                            <textarea id="otherReason" class="form-control mt-2" 
                                placeholder="Vui lòng nhập lý do khác..." 
                                style="display: none;"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                        <button type="button" class="btn btn-danger" id="confirmCancel">Xác nhận hủy</button>
                    </div>
                </div>
            </div>
        </div>
    `;

        // Thêm modal vào body
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Lấy reference đến modal
        const modalElement = document.getElementById('cancelOrderModal');
        const modal = new bootstrap.Modal(modalElement);

        // Xử lý sự kiện khi chọn reason
        const selectElement = document.getElementById('cancelReason');
        const textareaElement = document.getElementById('otherReason');

        selectElement.addEventListener('change', (e) => {
            textareaElement.style.display = e.target.value === 'OTHER' ? 'block' : 'none';
        });

        // Xử lý sự kiện xác nhận hủy
        document.getElementById('confirmCancel').addEventListener('click', () => {
            const reason = selectElement.value;
            const otherReason = textareaElement.value;

            if (!reason) {
                Toastify({
                    text: "Vui lòng chọn lý do hủy đơn",
                    duration: 3000,
                    close: true,
                    gravity: "top",
                    position: "right",
                    style: {
                        background: "#ff6b6b",
                    }
                }).showToast();
                return;
            }

            if (reason === 'OTHER' && !otherReason.trim()) {
                Toastify({
                    text: "Vui lòng nhập lý do cụ thể",
                    duration: 3000,
                    close: true,
                    gravity: "top",
                    position: "right",
                    style: {
                        background: "#ff6b6b",
                    }
                }).showToast();
                return;
            }

            const cancelData = {
                orderId: orderId,
                reason: reason,
                otherReason: otherReason
            };

            $http.patch('/api/order/cancel/' + orderId, cancelData)
                .then(function(response) {
                    showToast(
                        'Đơn hàng đã được hủy thành công',
                        'success',
                        'Hủy đơn hàng thành công!'
                    );

                    modal.hide();
                    modalElement.remove();
                    $scope.loadOrderHistory();
                })
                .catch(function(error) {
                    showToast(
                        'Không thể hủy đơn hàng. ' + (error.data?.error || ''),
                        'error',
                        'Lỗi hệ thống!'
                    );
                    console.error('Error canceling order:', error);
                });
        });

        // Xử lý sự kiện khi modal đóng
        modalElement.addEventListener('hidden.bs.modal', () => {
            modalElement.remove();
        });

        // Hiển thị modal
        modal.show();
    };



    // Gọi hàm tải danh sách đơn hàng khi controller được khởi tạo
    $scope.loadOrderHistory();
});


app.controller('AddressController', function($scope, $http) {
    $scope.addresses = [];
    $scope.currentAddress = {}; // Địa chỉ đang được thêm hoặc chỉnh sửa
    $scope.isEditing = false; // Trạng thái để xác định xem form đang ở chế độ thêm hay chỉnh sửa

    // Lấy danh sách địa chỉ
    $scope.loadAddresses = function() {
        $http.get('/api/addresses?userId=1').then(function(response) {
            $scope.addresses = response.data;
        });
    };

    // Thêm địa chỉ mới
    $scope.addAddress = function() {
        // Đặt tất cả các địa chỉ hiện tại thành không mặc định
        $scope.addresses.forEach(function(address) {
            address.isDefault = false;
        });

        // Thiết lập địa chỉ mới là mặc định
        $scope.currentAddress.isDefault = true;

        // Gửi yêu cầu thêm địa chỉ
        $http.post('/api/addresses', $scope.currentAddress).then(function(response) {
            $scope.addresses.push(response.data);
            $scope.currentAddress = {};
            showToast(
                'Địa chỉ mới đã được thêm thành công',
                'success',
                'Thêm địa chỉ thành công!'
            );
        })
            .catch(function(error) {
                showToast(
                    'Không thể thêm địa chỉ mới',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
    };

    // Đặt địa chỉ mặc định
    $scope.setDefault = function(addressId) {
        $http.put('/api/addresses/' + addressId + '/set-default?userId=1').then(function() {
            // Cập nhật danh sách địa chỉ sau khi đặt mặc định
            $scope.addresses.forEach(function(address) {
                address.isDefault = (address.id === addressId);
            });
            showToast(
                'Đã cập nhật địa chỉ mặc định',
                'success',
                'Cập nhật thành công!'
            );
        })
            .catch(function(error) {
                showToast(
                    'Không thể đặt địa chỉ mặc định',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
    };

    // Chỉnh sửa địa chỉ
    $scope.editAddress = function(address) {
        $scope.currentAddress = angular.copy(address); // Sao chép địa chỉ để chỉnh sửa
        $scope.isEditing = true; // Đặt trạng thái là chỉnh sửa
    };

    // Cập nhật địa chỉ
    $scope.updateAddress = function() {
        $http.put('/api/addresses/' + $scope.currentAddress.id, $scope.currentAddress).then(function(response) {
            const index = $scope.addresses.findIndex(addr => addr.id === response.data.id);
            if (index !== -1) {
                $scope.addresses[index] = response.data; // Cập nhật địa chỉ trong danh sách
            }
            $scope.cancelEdit(); // Reset form chỉnh sửa
            showToast(
                'Địa chỉ đã được cập nhật thành công',
                'success',
                'Cập nhật thành công!'
            );
        })
            .catch(function(error) {
                showToast(
                    'Không thể cập nhật địa chỉ',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
    };

    // Xóa địa chỉ
    $scope.deleteAddress = function(addressId) {
        $http.delete('/api/addresses/' + addressId).then(function() {
            $scope.addresses = $scope.addresses.filter(addr => addr.id !== addressId); // Xóa địa chỉ khỏi danh sách
            showToast(
                'Địa chỉ đã được xóa thành công',
                'success',
                'Xóa địa chỉ thành công!'
            );
        })
            .catch(function(error) {
                showToast(
                    'Không thể xóa địa chỉ',
                    'error',
                    'Lỗi hệ thống!'
                );
            });
    };

    // Hủy chỉnh sửa
    $scope.cancelEdit = function() {
        $scope.currentAddress = {}; // Reset form
        $scope.isEditing = false; // Đặt lại trạng thái
    };

    // Tải danh sách địa chỉ khi trang được tải
    $scope.loadAddresses();
});













